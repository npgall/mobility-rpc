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
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
            final ConnectionId threadLocalConnectionId = threadLocalConnectionIds.get();
            if (threadLocalConnectionId == null) {
                throw new ClassNotFoundException("No thread-local connection id is registered for the thread requesting class: " + name);
            }

            // Convert class name to resource name...
            String resourceName = name.replace('.', '/') + ".class";
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
            byte[] requiredResourceData = null;
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

    

    @Override
    public InputStream getResourceAsStream(String name) {
        byte[] cachedResourceData = resourceDataCache.get(name);
        if (cachedResourceData != null) {
            return new ByteArrayInputStream(cachedResourceData);
        }
        return super.getResourceAsStream(name);
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
