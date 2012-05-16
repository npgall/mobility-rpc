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

import com.googlecode.mobilityrpc.controller.MobilityController;
import com.googlecode.mobilityrpc.controller.impl.MobilityControllerInternal;
import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.protocol.pojo.*;
import com.googlecode.mobilityrpc.quickstart.EmbeddedMobilityServer;
import com.googlecode.mobilityrpc.serialization.Serializer;
import com.googlecode.mobilityrpc.serialization.impl.KryoSerializer;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class MobilitySessionImpl implements MobilitySessionInternal {

    /**
     * How long in millis threads waiting for an execution response to arrive should wait before giving up.
     */
    private static final long EXECUTION_RESPONSE_TIMEOUT_MILLIS = 60000;

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final UUID sessionId;
    private final MobilityControllerInternal mobilityController;
    private final SessionClassLoader sessionClassLoader;
    private final Serializer defaultSerializer;
    private final SerializationFormat defaultSerializationFormat;

    private final ConcurrentMap<RequestIdentifier, FutureExecutionResponse> futureExecutionResponses = new ConcurrentHashMap<RequestIdentifier, FutureExecutionResponse>();

    private final AtomicInteger numRemoteThreadsExecutingInThisSession = new AtomicInteger(); // TODO: ..use Semaphore instead?
    private volatile boolean sessionReleaseRequested = false;

    public MobilitySessionImpl(UUID sessionId, MobilityControllerInternal mobilityController) {
        this.sessionId = sessionId;
        this.mobilityController = mobilityController;
        this.sessionClassLoader = new SessionClassLoader(mobilityController, sessionId);
        this.defaultSerializer = new KryoSerializer(sessionClassLoader);
        this.defaultSerializationFormat = SerializationFormat.KRYO;
    }

    @Override
    public UUID getSessionId() {
        return sessionId;
    }

    @Override
    public void execute(String address, Runnable runnable) {
        execute(new ConnectionId(address, EmbeddedMobilityServer.DEFAULT_PORT), runnable);
    }

    @Override
    public void execute(ConnectionId connectionId, Runnable runnable) {
        execute(connectionId, ExecutionMode.RETURN_RESPONSE, runnable);
    }

    @Override
    public void execute(ConnectionId connectionId, ExecutionMode executionMode, Runnable runnable) {
        // Serialize the object...
        final byte[] serializedExecutableObject = serialize(runnable, defaultSerializationFormat);

        // Prepare an ExecutionRequest object which we will send to remote machine...
        RequestIdentifier requestIdentifier = new RequestIdentifier(sessionId, UUID.randomUUID(), null);
        ExecutionRequest outgoingRequest = new ExecutionRequest(
                serializedExecutableObject,
                defaultSerializationFormat,
                executionMode,
                requestIdentifier
        );
        switch (executionMode) {
            case FIRE_AND_FORGET:
                // No need to block waiting for response.

                // Send execution request to remote machine, and then return without blocking...
                try {
                    mobilityController.sendOutgoingMessage(connectionId, outgoingRequest);
                }
                catch (Exception e) {
                    // This exception is unlikely, should only occur if our outgoing queue to machine specified is full...
                    throw new IllegalStateException("Failed to submit Runnable object in FIRE_AND_FORGET mode for execution on remote machine: " + connectionId, e);
                }
                break;
            case RETURN_RESPONSE:
                // Send request and block waiting for response from remote machine.

                final ExecutionResponse executionResponse;
                try {
                    // Register a FutureExecutionResponse object in the map, which the thread processing a response to this
                    // request can later look up to notify this thread of the outcome of executing the request...
                    FutureExecutionResponse futureExecutionResponse = new FutureExecutionResponse(requestIdentifier);
                    futureExecutionResponses.put(requestIdentifier, futureExecutionResponse);

                    // Send the execution request to the remote machine...
                    mobilityController.sendOutgoingMessage(connectionId, outgoingRequest);

                    // Now block this thread until we get a response, or we time out...
                    executionResponse = futureExecutionResponse.getResponse(EXECUTION_RESPONSE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                }
                catch (Exception e) {
                    throw new IllegalStateException("Failed to receive response for execution request sent to remote machine in RETURN_RESPONSE mode for request identifier: " + requestIdentifier + ", connection id: " + connectionId, e);
                }

                // Decipher the execution response and return control normally to the client,
                // or throw exception as necessary...
                final ExecutionResponse.ExecutionOutcome executionOutcome = executionResponse.getExecutionOutcome();

                // Indicate to the class loader that should this thread require classes when deserializing
                // the response that they can be obtained from this remote machine...
                sessionClassLoader.setThreadLocalConnectionId(connectionId);
                try {
                    switch (executionOutcome) {
                        case VOID_RETURNED:
                            // Return normally...
                            return;
                        case FAILURE:
                            // The code threw an exception on the remote machine.
                            // Deserialize the exception and throw it to the caller on this machine...
                            Object responseObject = deserialize(executionResponse.getSerializedReturnObject(), executionResponse.getSerializationFormat());
                            // Sanity check to validate that indeed an exception was serialized as expected...
                            if (!(responseObject instanceof Throwable)) {
                                throw new IllegalStateException("Unexpected response object returned for execution outcome FAILURE: " + responseObject);
                            }
                            throw new IllegalStateException("An exception was thrown by the Runnable object when executed on the remote machine: " + connectionId, (Throwable)responseObject);
                        case VALUE_RETURNED:
                            // A Runnable does not return a value, this would indicate a problem with the framework...
                            throw new IllegalStateException("Unexpected ExecutionOutcome returned: " + executionMode);
                        default:
                            throw new IllegalStateException("Unexpected ExecutionOutcome returned: " + executionMode);
                    }
                }
                finally {
                    // Null-out the connection id for this calling thread,
                    // now that response has been deserialized...
                    sessionClassLoader.setThreadLocalConnectionId(null);
                }
            default:
                throw new IllegalStateException("Unexpected ExecutionMode specified: " + executionMode);
        }
    }

    @Override
    public <T> T execute(String address, Callable<T> callable) {
        return execute(new ConnectionId(address, EmbeddedMobilityServer.DEFAULT_PORT), callable);
    }

    @Override
    public <T> T execute(ConnectionId connectionId, Callable<T> callable) {
        return execute(connectionId, ExecutionMode.RETURN_RESPONSE, callable);
    }

    @Override
    public <T> T execute(ConnectionId connectionId, ExecutionMode executionMode, Callable<T> callable) {
        // Serialize the object...
        final byte[] serializedExecutableObject = serialize(callable, defaultSerializationFormat);

        // Prepare an ExecutionRequest object which we will send to remote machine...
        RequestIdentifier requestIdentifier = new RequestIdentifier(sessionId, UUID.randomUUID(), null);
        ExecutionRequest outgoingRequest = new ExecutionRequest(
                serializedExecutableObject,
                defaultSerializationFormat,
                executionMode,
                requestIdentifier
        );
        switch (executionMode) {
            case FIRE_AND_FORGET:
                // No need to block waiting for response.

                // Send execution request to remote machine, and then return without blocking...
                try {
                    mobilityController.sendOutgoingMessage(connectionId, outgoingRequest);
                }
                catch (Exception e) {
                    // This exception is unlikely, should only occur if our outgoing queue to machine specified is full...
                    throw new IllegalStateException("Failed to submit Callable object in FIRE_AND_FORGET mode for execution on remote machine: " + connectionId, e);
                }
                return null;
            case RETURN_RESPONSE:
                // Send request and block waiting for response from remote machine.

                final ExecutionResponse executionResponse;
                try {
                    // Register a FutureExecutionResponse object in the map, which the thread processing a response to this
                    // request can later look up to notify this thread of the outcome of executing the request...
                    FutureExecutionResponse futureExecutionResponse = new FutureExecutionResponse(requestIdentifier);
                    futureExecutionResponses.put(requestIdentifier, futureExecutionResponse);

                    // Send the execution request to the remote machine...
                    mobilityController.sendOutgoingMessage(connectionId, outgoingRequest);

                    // Now block this thread until we get a response, or we time out...
                    executionResponse = futureExecutionResponse.getResponse(EXECUTION_RESPONSE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                }
                catch (Exception e) {
                    throw new IllegalStateException("Failed to receive response for execution request sent to remote machine in RETURN_RESPONSE mode for request identifier: " + requestIdentifier + ", connection id: " + connectionId, e);
                }

                // Decipher the execution response and return control normally to the client,
                // or throw exception as necessary...
                final ExecutionResponse.ExecutionOutcome executionOutcome = executionResponse.getExecutionOutcome();

                // Indicate to the class loader that should this thread require classes when deserializing
                // the response that they can be obtained from this remote machine...
                sessionClassLoader.setThreadLocalConnectionId(connectionId);
                try {
                    switch (executionOutcome) {
                        case VOID_RETURNED:
                            // Return normally...
                            return null;
                        case FAILURE:
                            // The code threw an exception on the remote machine.
                            // Deserialize the exception and throw it to the caller on this machine...
                            Object throwable = deserialize(executionResponse.getSerializedReturnObject(), executionResponse.getSerializationFormat());
                            // Sanity check to validate that indeed an exception was serialized as expected...
                            if (!(throwable instanceof Throwable)) {
                                throw new IllegalStateException("Unexpected response object returned for execution outcome FAILURE: " + throwable);
                            }
                            throw new IllegalStateException("An exception was thrown by the Callable object when executed on the remote machine: " + connectionId, (Throwable)throwable);
                        case VALUE_RETURNED:
                            // The callable returned an object when executed on the remote machine, return it to
                            // the caller of this method...
                            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                            T objectReturned = (T) deserialize(executionResponse.getSerializedReturnObject(), executionResponse.getSerializationFormat());
                            return objectReturned;
                        default:
                            throw new IllegalStateException("Unexpected ExecutionOutcome returned: " + executionMode);
                    }
                }
                finally {
                    // Null-out the connection id for this calling thread,
                    // now that response has been deserialized...
                    sessionClassLoader.setThreadLocalConnectionId(null);
                }
            default:
                throw new IllegalStateException("Unexpected ExecutionMode specified: " + executionMode);
        }
    }

    public void receiveIncomingExecutionRequest(ConnectionId connectionId, ExecutionRequest executionRequest) {
        // Indicate to the class loader that should this thread require classes when processing this request
        // that the classes can be requested via the connection from which we received the request...
        sessionClassLoader.setThreadLocalConnectionId(connectionId);

        // Outer try-catch to catch and log all exceptions
        // so as not to kill a processing thread on a bad request...
        try {
            numRemoteThreadsExecutingInThisSession.incrementAndGet();
            // Inner try-catch to catch unexpected exceptions
            // and add additional context information to exception messages...
            try {
                // Deserialize the Runnable or Callable object sent by the client,
                // using a (de)serializer appropriate to the format indicated in the request...
                byte[] serializeExecutableObject = executionRequest.getSerializedExecutableObject();
                final SerializationFormat serializationFormat = executionRequest.getSerializationFormat();
                final Object executableObject = deserialize(serializeExecutableObject, serializationFormat);

                Throwable exceptionThrown = null;
                Object objectReturned = null;
                try {
                    // Set the current session details into a thread-local variable, so code can access its own session...
                    MobilityContextInternal.setCurrentSession(this);
                    MobilityContextInternal.setCurrentConnectionId(connectionId);

                    // Determine if object is Runnable or Callable...
                    if (executableObject instanceof Runnable) {
                        // Execute as Runnable...
                        Runnable runnable = (Runnable) executableObject;
                        runnable.run();
                    }
                    else if (executableObject instanceof Callable) {
                        Callable callable = (Callable) executableObject;
                        objectReturned = callable.call();
                    }
                    else {
                        throw new IllegalStateException("Unexpected type of deserialized executable object, expected Runnable or Callable: " + (executableObject == null ? null : executableObject.getClass().getName()));
                    }
                }
                catch (Throwable e) {
                    // Catch Throwable, because we have no idea what client-supplied code might throw...
                    exceptionThrown = e;
                }
                finally {
                    // Unset current session details from the thread-local variable...
                    MobilityContextInternal.setCurrentSession(null);
                    MobilityContextInternal.setCurrentConnectionId(null);
                }
                final ExecutionResponse executionResponse;
                switch (executionRequest.getExecutionMode()) {
                    case FIRE_AND_FORGET:
                        // No need to send response to client.
                        if (logger.isLoggable(Level.FINER)) {
                            logger.log(Level.FINER, "Processed execution task and skipped sending response to client, for connection id: " + connectionId + ", execution request: " + executionRequest);
                        }
                        break;
                    case RETURN_RESPONSE:
                        if (objectReturned != null) {
                            executionResponse = new ExecutionResponse(
                                    ExecutionResponse.ExecutionOutcome.VALUE_RETURNED,
                                    serialize(objectReturned, defaultSerializationFormat),
                                    defaultSerializationFormat,
                                    executionRequest.getRequestIdentifier()
                            );
                        }
                        else if (exceptionThrown != null) {
                            executionResponse = new ExecutionResponse(
                                    ExecutionResponse.ExecutionOutcome.FAILURE,
                                    serialize(exceptionThrown, defaultSerializationFormat),
                                    defaultSerializationFormat,
                                    executionRequest.getRequestIdentifier()
                            );
                        }
                        else {
                            executionResponse = new ExecutionResponse(
                                    ExecutionResponse.ExecutionOutcome.VOID_RETURNED,
                                    serialize(null, defaultSerializationFormat),
                                    defaultSerializationFormat,
                                    executionRequest.getRequestIdentifier()
                            );
                        }
                        mobilityController.sendOutgoingMessage(connectionId, executionResponse);
                        if (logger.isLoggable(Level.FINER)) {
                            logger.log(Level.FINER, "Processed execution task and sent response to client, for connection id: " + connectionId + ", execution request: " + executionRequest);
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected execution mode specified in request: " + executionRequest.getExecutionMode());
                }
            }
            catch (Exception e) {
                // Catch any exceptions above and simply re-throw them with additional context information...
                throw new IllegalStateException("Failed to process execution request, for connection id: " + connectionId + ", execution request: " + executionRequest, e);
            }
            finally {
                if (numRemoteThreadsExecutingInThisSession.decrementAndGet() == 0 && sessionReleaseRequested) {
                    // This is the last (or only) thread in this session processing a request from a remote machine.
                    // At least one of the mobile objects executed called MobilitySession.release(), which set the
                    // sessionReleaseRequested flag to true.
                    // Only now that the last remote thread is about to finish, do we release the session...
                    doRelease();
                    sessionReleaseRequested = false;
                    if (logger.isLoggable(Level.FINER)) {
                        logger.log(Level.FINER, "Processed deferred release of session, for connection id: " + connectionId + ", execution request: " + executionRequest);
                    }
                }
                else if (sessionReleaseRequested && logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Deferred release of session to another thread, for connection id: " + connectionId + ", execution request: " + executionRequest);
                }
            }
        }
        catch (Exception e) {
            // Note: Exceptions caught here are not exceptions thrown by the client-supplied code.
            // Exceptions thrown by client-supplied code are caught explicitly and returned to the client above.

            // Exceptions caught here would therefore indicate problems with the framework itself.
            // Log such exceptions at SEVERE level...
            logger.log(Level.SEVERE, "Unexpected exception processing execution task, for connection id: " + connectionId + ", execution request: " + executionRequest, e);
        }
        // Null-out the connection id for this thread, which we set earlier above...
        sessionClassLoader.setThreadLocalConnectionId(null);
    }


    public void receiveExecutionResponse(ExecutionResponse executionResponse) {
        RequestIdentifier requestIdentifier = executionResponse.getRequestIdentifier();
        FutureExecutionResponse futureExecutionResponse = futureExecutionResponses.get(requestIdentifier);
        if (futureExecutionResponse == null) {
            // Request must have timed out...
            logger.log(Level.FINER, "Ignored ExecutionResponse, no pending request found, request must have timed out: {0}", executionResponse);
            return;
        }
        futureExecutionResponse.setResponse(executionResponse);
        logger.log(Level.FINER, "Accepted ExecutionResponse, passed to request thread: {0}", executionResponse);
    }

    /**
     * Represents an {@link ExecutionResponse} which will materialize in the future.
     * <p/>
     * Note this would be similar to {@link java.util.concurrent.Future}&lt;ExecutionResponse&gt;, but we don't
     * implement {@code Future} because it would require us to implement more methods than we really need here.
     * <p/>
     * The a client thread calling into the {@link #execute} methods will will register this object in a map, then
     * send an {@link ExecutionRequest} object to a remote machine.
     * The client thread will then block on the {@link #getResponse} method of this object.
     * <p/>
     * When a {@link ExecutionResponse} arrives from the remote machine, the thread processing it will look up this
     * object in the map and call {@link #setResponse}. At that point the blocked client thread will receive the
     * ExecutionResponse and will continue its work.
     * <p/>
     * Note that only calls to the {@link #execute} methods which specify that responses are required will cause
     * the client thread to block.
     */
    class FutureExecutionResponse {
        private final RequestIdentifier requestIdentifier;

        private final BlockingQueue<ExecutionResponse> responseQueue = new ArrayBlockingQueue<ExecutionResponse>(1);

        FutureExecutionResponse(RequestIdentifier requestIdentifier) {
            this.requestIdentifier = requestIdentifier;
        }

        public ExecutionResponse getResponse(long timeout, TimeUnit unit) {
            try {
                ExecutionResponse executionResponse = responseQueue.poll(timeout, unit);
                if (executionResponse == null) {
                    throw new TimeoutException();
                }
                return executionResponse;
            }
            catch (TimeoutException e) {
                throw new IllegalStateException("Timed out waiting to receive execution response within timeout of " + timeout + " " + unit.name().toLowerCase(), e);
            }
            catch (Exception e) {
                throw new IllegalStateException("Unexpected exception waiting to receive execution response", e);
            }
            finally {
                futureExecutionResponses.remove(this.requestIdentifier);
            }
        }

        public boolean setResponse(ExecutionResponse executionResponse) {
            return responseQueue.add(executionResponse);
        }
    }

    @Override
    public SessionClassLoader getSessionClassLoader() {
        return sessionClassLoader;
    }

    @Override
    public MobilityController getMobilityController() {
        return this.mobilityController;
    }

    @Override
    public void release() {
        if (MobilityContextInternal.hasCurrentSession()) {
            // This is being called by a thread executing a request from a remote machine.
            // Defer releasing the session until all threads processing remote requests in this session have finished.
            // Set a flag to signal that last such thread to finish, should release the session...
            this.sessionReleaseRequested = true;
        }
        else {
            // This is being called by a thread from the local application.
            // Check if remote threads are executing in the session...
            if (numRemoteThreadsExecutingInThisSession.get() == 0) {
                // No remote threads are executing in this session, release the session...
                doRelease();
            }
            else {
                // Some remote threads are executing in the session,
                // set the flag to have those threads release the session...
                this.sessionReleaseRequested = true;
            }
        }
    }

    void doRelease() {
        mobilityController.releaseSession(this.sessionId);
    }

    private Object deserialize(byte[] serializedObject, SerializationFormat serializationFormat) {
        try {
            switch (serializationFormat) {
                case KRYO:
                    // Note: we only support one serialization format now,
                    // however the protocol allows for others in future...
                    return defaultSerializer.deserialize(serializedObject);
                default:
                    throw new IllegalStateException("Unsupported serialization format: " + serializationFormat);
            }
        }
        catch (Exception e) {
            throw new IllegalStateException("Exception deserializing object from " + serializedObject.length + " bytes data in " + serializationFormat + " format", e);
        }
    }

    private byte[] serialize(Object object, SerializationFormat serializationFormat) {
        try {
            switch (serializationFormat) {
                case KRYO:
                    return defaultSerializer.serialize(object);
                default:
                    throw new IllegalStateException("Unsupported serialization format: " + serializationFormat);
            }
        }
        catch (Exception e) {
            throw new IllegalStateException("Exception serializing object to " + serializationFormat + " format: " + object, e);
        }
    }

    @Override
    public String toString() {
        return "MobilitySession{" +
                "sessionId=" + sessionId +
                '}';
    }
}
