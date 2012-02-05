/**
 * Copyright 2011 Niall Gallagher
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

import com.googlecode.mobilityrpc.execution.impl.MessageHandlingExecutionCoordinator;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.ByteCodeRequest;
import com.googlecode.mobilityrpc.protocol.pojo.ByteCodeResponse;
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
     * How long in millis threads waiting for bytecode to arrive should wait before giving up.
     */
    private static final long BYTE_CODE_REQUEST_TIMEOUT_MILLIS = 10000;

    private final MessageHandlingExecutionCoordinator executionCoordinator;
    private final UUID sessionId;
    private final ThreadLocal<ConnectionIdentifier> threadLocalConnectionIdentifiers = new ThreadLocal<ConnectionIdentifier>();

    private final ConcurrentMap<RequestIdentifier, FutureByteCodeResponse> futureByteCodeResponses = new ConcurrentHashMap<RequestIdentifier, FutureByteCodeResponse>();

    private final Map<String, byte[]> byteCodeResourceCache = new ConcurrentHashMap<String, byte[]>();

    public SessionClassLoader(MessageHandlingExecutionCoordinator executionCoordinator, UUID sessionId) {
        super(SessionClassLoader.class.getClassLoader());
        this.executionCoordinator = executionCoordinator;
        this.sessionId = sessionId;
    }

    /**
     * Called by threads processing an execution request from a remote machine, to indicate to this class loader that
     * should those threads require classes to be loaded that the class loader can request those classes from the
     * remote machine indicated.
     * <p/>
     * When threads finish processing execution requests, they should call this method supplying {@code null} for the
     * connection identifier.
     *
     * @param connectionIdentifier Indicates the current connection in use by a thread processing an execution request
     */
    public void setThreadLocalConnectionIdentifier(ConnectionIdentifier connectionIdentifier) {
        threadLocalConnectionIdentifiers.set(connectionIdentifier);
    }

    /**
     * Called by the thread processing an incoming {@link ByteCodeResponse} object.
     * This method will look up and unblock the relevant thread which is waiting for the bytecode to arrive.
     *
     * @param byteCodeResponse Contains bytecode which this class loader previously requested from a remote machine
     * on behalf of a thread on this machine which tried to access a class which was not loaded.
     */
    public void processBytecodeResponse(ByteCodeResponse byteCodeResponse) {
        RequestIdentifier requestIdentifier = byteCodeResponse.getRequestIdentifier();
        FutureByteCodeResponse futureByteCodeResponse = futureByteCodeResponses.get(requestIdentifier);
        if (futureByteCodeResponse == null) {
            // Request must have timed out...
            logger.log(Level.FINE, "Ignored ByteCodeResponse, no pending request found, request must have timed out: {0}", byteCodeResponse);
            return;
        }
        futureByteCodeResponse.setResponse(byteCodeResponse);
        logger.log(Level.FINE, "Accepted ByteCodeResponse, passed to request thread: {0}", byteCodeResponse);
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
            final ConnectionIdentifier threadLocalConnectionIdentifier = threadLocalConnectionIdentifiers.get();
            if (threadLocalConnectionIdentifier == null) {
                throw new ClassNotFoundException("No thread-local connection identifier is registered for the thread requesting class: " + name);
            }

            // Wrap our required class in a singleton list.
            // Note our protocol intentionally supports requesting a list of classes at once as an optimization,
            // however implementing this optimization is reserved for future work.
            List<String> requiredClasses = Collections.singletonList(name);

            // Send a request to the remote machine for the required class(es)...
            FutureByteCodeResponse futureResponse = sendRequestForByteCode(requiredClasses);

            // Block here until the response arrives, or we time out...
            ByteCodeResponse byteCodeResponse = futureResponse.getResponse(BYTE_CODE_REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

            // Search the potentially multiple classes returned for the class we need.
            // Note: again, the protocol supports returning multiple classes,
            // however implementing this optimization is reserved for future work.
            byte[] requiredClassData = null;
            for (ByteCodeResponse.ClassData classData : byteCodeResponse.getByteCodeResponses()) {
                if (name.equals(classData.getClassName())) {
                    requiredClassData = classData.getByteCode();
                    byteCodeResourceCache.put(name.replace('.', '/') + ".class", requiredClassData);
                    break;
                }
            }

            if (requiredClassData == null) {
                // The remote machine returned a response but it did not include the required class,
                // i.e. it could not locate the required class (this is an unexpected condition)...
                throw new ClassNotFoundException("The client could not locate bytecode for the requested class: " + name);
            }
            return defineClass(name, requiredClassData, 0, requiredClassData.length);
        }
        catch (Throwable t) {
            throw new ClassNotFoundException("Could not locate bytecode for the requested class: " + name, t);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        byte[] cachedByteCode = byteCodeResourceCache.get(name);
        if (cachedByteCode != null) {
            return new ByteArrayInputStream(cachedByteCode);
        }
        return super.getResourceAsStream(name);
    }

    /**
     * A helper method for {@link #findClass}, this method actually sends a request for the specified classes to
     * remote machine(s).
     * @param requestedClasses A list of classes required
     * @return An object which the calling method can block on, which will return bytecode when it arrives in a response
     * from the remote machine
     */
    FutureByteCodeResponse sendRequestForByteCode(List<String> requestedClasses) {
        final ConnectionIdentifier threadLocalConnectionIdentifier = threadLocalConnectionIdentifiers.get();
        if (threadLocalConnectionIdentifier == null) {
            throw new IllegalStateException("No thread-local connection identifier is registered for the thread requesting classes: " + requestedClasses);
        }
        // Create a unique RequestIdentifier for the ByteCodeRequest we will send...
        UUID requestId = UUID.randomUUID();
        RequestIdentifier requestIdentifier = new RequestIdentifier(sessionId, requestId, "Request for classes: " + requestedClasses);

        // Register a FutureByteCodeResponse in the futureByteCodeResponses map...
        FutureByteCodeResponse futureByteCodeResponse = new FutureByteCodeResponse(requestIdentifier);
        futureByteCodeResponses.put(requestIdentifier, futureByteCodeResponse);

        // Send a ByteCodeRequest to the remote machine...
        ByteCodeRequest byteCodeRequest = new ByteCodeRequest(requestedClasses, requestIdentifier);
        executionCoordinator.sendOutgoingMessage(threadLocalConnectionIdentifier, byteCodeRequest);

        // Return our FutureByteCodeResponse object, which the calling method can block on until response arrives...
        return futureByteCodeResponse;
    }


    /**
     * Represents a {@link ByteCodeResponse} which will materialize in the future.
     * <p/>
     * Note this would be similar to {@link java.util.concurrent.Future}&lt;ByteCodeResponse&gt;, but we don't
     * implement {@code Future} because it would require us to implement more methods than we really need here.
     * <p/>
     *
     * The local thread requiring bytecode will register this object in a map, then send a request for the bytecode to a
     * remote machine.
     * The local thread will then block on the {@link #getResponse} method of this object.
     * <p/>
     * When a {@link ByteCodeResponse} arrives from the remote machine, the thread processing it will look up this
     * object in the map and call {@link #setResponse}. At that point the blocked local thread will receive the
     * ByteCodeResponse and will continue its work.
     */
    class FutureByteCodeResponse {
        private final RequestIdentifier requestIdentifier;

        private final BlockingQueue<ByteCodeResponse> responseQueue = new ArrayBlockingQueue<ByteCodeResponse>(1);

        FutureByteCodeResponse(RequestIdentifier requestIdentifier) {
            this.requestIdentifier = requestIdentifier;
        }

        public ByteCodeResponse getResponse(long timeout, TimeUnit unit) {
            try {
                ByteCodeResponse byteCodeResponse = responseQueue.poll(timeout, unit);
                if (byteCodeResponse == null) {
                    throw new TimeoutException();
                }
                return byteCodeResponse;
            }
            catch (TimeoutException e) {
                throw new IllegalStateException("Timed out waiting to receive byte code response within timeout of " + timeout + " " + unit.name().toLowerCase(), e);
            }
            catch (Exception e) {
                throw new IllegalStateException("Unexpected exception waiting to receive byte code response", e);
            }
            finally {
                futureByteCodeResponses.remove(this.requestIdentifier);
            }
        }

        public boolean setResponse(ByteCodeResponse byteCodeResponse) {
            return responseQueue.add(byteCodeResponse);
        }
    }
}
