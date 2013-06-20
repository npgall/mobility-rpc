/**
 * Copyright 2011, 2012 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mobilityrpc.session.impl;

import com.googlecode.mobilityrpc.controller.impl.MobilityControllerInternal;
import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.protocol.pojo.ResourceRequest;
import com.googlecode.mobilityrpc.protocol.pojo.ResourceResponse;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class SessionClassLoader extends ClassLoader {

    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * How long in millis threads waiting for bytecode or classpath resources to arrive should wait before giving up.
     */
    private static final long RESOURCE_REQUEST_TIMEOUT_MILLIS = 10000;

    private final MobilityControllerInternal mobilityController;
    private final UUID sessionId;
    private final ThreadLocal<ConnectionId> threadLocalConnectionIds = new ThreadLocal<ConnectionId>();

    private final ConcurrentMap<RequestIdentifier, FutureResourceResponse> futureResourceResponses = new ConcurrentHashMap<RequestIdentifier, FutureResourceResponse>();

    private final Map<String, byte[]> resourceDataCache = new ConcurrentHashMap<String, byte[]>();

    public SessionClassLoader(MobilityControllerInternal mobilityController, UUID sessionId) {
        super(SessionClassLoader.class.getClassLoader());
        this.mobilityController = mobilityController;
        this.sessionId = sessionId;
    }

    /**
     * Called by threads processing an execution request from a remote machine, to indicate to this class loader that
     * should those threads require classes to be loaded that the class loader can request those classes from the
     * remote machine indicated.
     * <p/>
     * When threads finish processing execution requests, they should call this method supplying {@code null} for the
     * connection id.
     *
     * @param connectionId Indicates the current connection in use by a thread processing an execution request
     */
    public void setThreadLocalConnectionId(ConnectionId connectionId) {
        threadLocalConnectionIds.set(connectionId);
    }

    /**
     * Called by the thread processing an incoming {@link ResourceResponse} object.
     * This method will look up and unblock the relevant thread which is waiting for the resource to arrive.
     *
     * @param resourceResponse Contains class bytecode or other resource which this class loader previously requested
     * from a remote machine on behalf of a thread on this machine which tried to access a class/resource which was not
     * loaded.
     */
    public void processResourceResponse(ResourceResponse resourceResponse) {
        RequestIdentifier requestIdentifier = resourceResponse.getRequestIdentifier();
        FutureResourceResponse futureResourceResponse = futureResourceResponses.get(requestIdentifier);
        if (futureResourceResponse == null) {
            // Request must have timed out...
            logger.log(Level.FINE, "Ignored ResourceResponse, no pending request found, request must have timed out: {0}", resourceResponse);
            return;
        }
        futureResourceResponse.setResponse(resourceResponse);
        logger.log(Level.FINE, "Accepted ResourceResponse, passed to request thread: {0}", resourceResponse);
    }

    /**
     * Tries to find classes by requesting bytecode from remote machines. This method will be called by the superclass
     * implementation of {@link #loadClass} when the parent class loader cannot locate the required class according
     * to the parent delegation model of class loading.
     *
     * @param name The binary name of the class required
     * @return The requested class, freshly loaded into the local JVM by requesting its bytecode from a remote machine
     * @throws ClassNotFoundException If the requested class cannot be located on remote machines
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            // Convert class name to resource name...
            String resourceName = name.replace('.', '/') + ".class";

            // Retrieve resource from cache...
            byte[] requiredResourceData = resourceDataCache.get(resourceName);
            if (requiredResourceData != null) {
                return defineClass(name, requiredResourceData, 0, requiredResourceData.length);
            }

            // Not cached.

            // Check if we are executing code from a client and so if we should request resource from the client...
            final ConnectionId threadLocalConnectionId = threadLocalConnectionIds.get();
            if (threadLocalConnectionId == null) {
                // Connection id is null, therefore we are not executing/deserializing code from a client.
                // Therefore this method must have been called called by ResourceRequestMessageProcessor, or by other
                // code/thread in the local application. Therefore we keep the search local to this machine...
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "No bytecode cached for class: " + name);
                }
                throw new ClassNotFoundException("No bytecode previously cached for this class");
            }

            // We have a connection, therefore we are executing/deserializing code from a client.
            // Request the resource from the client...

            // Wrap our required class in a singleton list.
            // Note the protocol intentionally supports requesting a list of classes at once as an optimization,
            // however this optimization has not yet been implemented.
            List<String> requiredClasses = Collections.singletonList(resourceName);

            // Send a request to the remote machine for the required class(es)...
            FutureResourceResponse futureResponse = sendResourceRequest(requiredClasses);

            // Block here until the response arrives, or we time out...
            ResourceResponse resourceResponse = futureResponse.getResponse(RESOURCE_REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

            // Search the potentially multiple classes returned for the class we need.
            // Note: again, the protocol supports returning multiple classes,
            // however implementing this optimization is reserved for future work.
            for (ResourceResponse.ResourceData resourceData : resourceResponse.getResourceDataResponses()) {
                if (resourceName.equals(resourceData.getResourceName())) {
                    requiredResourceData = resourceData.getResourceData();
                    resourceDataCache.put(resourceName, requiredResourceData);
                    break;
                }
            }

            if (requiredResourceData == null) {
                // The remote machine returned a response but it did not include the required class,
                // i.e. it could not locate the required class (this is an unexpected condition)...
                throw new ClassNotFoundException("The remote machine could not locate bytecode for the requested class: " + name + ", resource name: " + resourceName);
            }
            return defineClass(name, requiredResourceData, 0, requiredResourceData.length);
        }
        catch (Throwable t) {
            throw new ClassNotFoundException("Could not locate bytecode for the requested class: " + name, t);
        }
    }

    /**
     * Tries to find resources by requesting requesting from remote machines. This method will be called by the
     * superclass implementation of {@link #getResource(String)} when the parent class loader cannot locate the required
     * resource according to the parent delegation model of class loading.
     * <p/>
     * This method caches all resources loaded from client machines, and will serve from this cache whenever possible.
     * <p/>
     * The {@link java.net.URL#openStream()} method of the URL returned, will provide the resource data.
     * <p/>
     * The <code>toString</code> representation of the URL will resemble the following:<br/>
     * <code>﻿mobility-rpc://[192.168.56.1:52671:0]/5f088ec8-4f71-4fae-a89b-56a0b408dcbe/test-resource.txt</code>
     * (when referencing a remote machine)<br/>
     * <code>﻿mobility-rpc://[local-cache:0:0]/5f088ec8-4f71-4fae-a89b-56a0b408dcbe/test-resource.txt</code>
     * (when subsequently cached locally)<br/>
     * Note that the string representation is for debugging purposes only.
     * The JVM will not be able to parse the URL from this string. The URL <i>object</i> returned however, is able to
     * provide the binary content of the resource.
     *
     * @param name The name of the resource required
     * @return A URL whose {@link java.net.URL#openStream()} method provides content for the given resource. Returns
     * <code>null</code> if the resource could not be found
     */
    @Override
    protected URL findResource(String name) {
        try {
            // Retrieve resource from cache...
            byte[] requiredResourceData = resourceDataCache.get(name);
            if (requiredResourceData != null) {
                return wrapAsUrl("local-cache:0:0", sessionId.toString(), name, requiredResourceData);
            }

            // Not cached.

            // Check if we are executing code from a client and so if we should request resource from the client...
            final ConnectionId threadLocalConnectionId = threadLocalConnectionIds.get();
            if (threadLocalConnectionId == null) {
                // Connection id is null, therefore we are not executing/deserializing code from a client.
                // Therefore this method must have been called called by ResourceRequestMessageProcessor, or by other
                // code/thread in the local application. Therefore we keep the search local to this machine...
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "No content cached for resource: " + name);
                }
                return null;
            }

            // We have a connection, therefore we are executing/deserializing code from a client.
            // Request the resource from the client...

            // Wrap our required resource in a singleton list.
            // Note the protocol intentionally supports requesting a list of resources at once as an optimization,
            // however this optimization has not yet been implemented.
            List<String> requiredResources = Collections.singletonList(name);

            // Send a request to the remote machine for the required resource(s)...
            FutureResourceResponse futureResponse = sendResourceRequest(requiredResources);

            // Block here until the response arrives, or we time out...
            ResourceResponse resourceResponse = futureResponse.getResponse(RESOURCE_REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

            // Search the potentially multiple resources returned for the resource we need...
            for (ResourceResponse.ResourceData resourceData : resourceResponse.getResourceDataResponses()) {
                if (name.equals(resourceData.getResourceName())) {
                    requiredResourceData = resourceData.getResourceData();
                    resourceDataCache.put(name, requiredResourceData);
                    break;
                }
            }

            if (requiredResourceData == null) {
                // The remote machine returned a response but it did not include the required resource...
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "The remote machine could not locate the requested resource: " + name);
                }
                return null;
            }
            return wrapAsUrl(threadLocalConnectionId.toString(), sessionId.toString(), name, requiredResourceData);
        }
        catch (Throwable t) {
            throw new IllegalStateException("Unexpected exception locating the requested resource: " + name, t);
        }
    }

    /**
     * Finds resources with the given name which are loadable by this session class loader.
     * <p/>
     * This will return either one URL or zero URLs. This method delegates to {@link #findResource(String)}.
     *
     * @param name The name of the resource
     * @return either one URL or zero URLs providing access to a resource with the given name
     */
    @Override
    protected Enumeration<URL> findResources(String name) {
        URL resourceUrl = findResource(name);
        return resourceUrl == null
                ? Collections.enumeration(Collections.<URL>emptySet())
                : Collections.enumeration(Collections.<URL>singleton(resourceUrl));
    }

    /**
     * Returns a URL which provides the given resource data as a stream.
     *
     * @param connectionId The connection from which the resource originated
     * @param sessionId id of the session on this machine and the client machine
     * @param resourceName The name of the resource on the classpath
     * @param resourceData The resource data to wrap
     * @return a URL which provides the given resource data as a stream
     */
    static URL wrapAsUrl(String connectionId, String sessionId, String resourceName, final byte[] resourceData) {
        try {
            return new URL("mobility-rpc", connectionId, -1, "/" + sessionId + "/" + resourceName, new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    return new URLConnection(u) {
                        @Override
                        public void connect() throws IOException {
                            // No op
                        }

                        @Override
                        public Object getContent() throws IOException {
                            return getInputStream();
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return new ByteArrayInputStream(resourceData);
                        }
                    };
                }
            });
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to wrap resource as URL: connection: " + connectionId + ", session: " + sessionId + ", resource: "  + resourceName, e);
        }
    }

    /**
     * A helper method for {@link #findClass}, this method actually sends a request for the specified classes byteode or
     * resources to remote machines.
     *
     * @param requestedResources A list of the names of classes or resources required
     * @return An object which the calling method can block on, which will return bytecode/resources when it arrives in
     * a response from the remote machine
     */
    FutureResourceResponse sendResourceRequest(List<String> requestedResources) {
        final ConnectionId threadLocalConnectionId = threadLocalConnectionIds.get();
        if (threadLocalConnectionId == null) {
            throw new IllegalStateException("No thread-local connection id is registered for the thread requesting classes: " + requestedResources);
        }
        // Create a unique RequestIdentifier for the ResourceRequest we will send...
        UUID requestId = UUID.randomUUID();
        RequestIdentifier requestIdentifier = new RequestIdentifier(sessionId, requestId, "Request for resources: " + requestedResources);

        // Register a FutureResourceResponse in the futureResourceResponses map...
        FutureResourceResponse futureResourceResponse = new FutureResourceResponse(requestIdentifier);
        futureResourceResponses.put(requestIdentifier, futureResourceResponse);

        // Send a ResourceRequest to the remote machine...
        ResourceRequest resourceRequest = new ResourceRequest(requestedResources, requestIdentifier);
        mobilityController.sendOutgoingMessage(threadLocalConnectionId, resourceRequest);

        // Return our FutureResourceResponse object, which the calling method can block on until response arrives...
        return futureResourceResponse;
    }


    /**
     * Represents a {@link ResourceResponse} which will materialize in the future.
     * <p/>
     * Note this would be similar to {@link java.util.concurrent.Future}&lt;ResourceResponse&gt;, but we don't
     * implement {@code Future} because it would require us to implement more methods than we really need here.
     * <p/>
     *
     * The local thread requiring bytecode/resource data will register this object in a map, then send a request for the
     * bytecode to a remote machine.
     * The local thread will then block on the {@link #getResponse} method of this object.
     * <p/>
     * When a {@link ResourceResponse} arrives from the remote machine, the thread processing it will look up this
     * object in the map and call {@link #setResponse}. At that point the blocked local thread will receive the
     * ResourceResponse and will continue its work.
     */
    class FutureResourceResponse {
        private final RequestIdentifier requestIdentifier;

        private final BlockingQueue<ResourceResponse> responseQueue = new ArrayBlockingQueue<ResourceResponse>(1);

        FutureResourceResponse(RequestIdentifier requestIdentifier) {
            this.requestIdentifier = requestIdentifier;
        }

        public ResourceResponse getResponse(long timeout, TimeUnit unit) {
            try {
                ResourceResponse resourceResponse = responseQueue.poll(timeout, unit);
                if (resourceResponse == null) {
                    throw new TimeoutException();
                }
                return resourceResponse;
            }
            catch (TimeoutException e) {
                throw new IllegalStateException("Timed out waiting to receive class bytecode or resource within timeout of " + timeout + " " + unit.name().toLowerCase(), e);
            }
            catch (Exception e) {
                throw new IllegalStateException("Unexpected exception waiting to receive bytecode or resource", e);
            }
            finally {
                futureResourceResponses.remove(this.requestIdentifier);
            }
        }

        public boolean setResponse(ResourceResponse resourceResponse) {
            return responseQueue.add(resourceResponse);
        }
    }
}
