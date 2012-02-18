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
package com.googlecode.mobilityrpc.controller.impl;

import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.network.impl.ConnectionManagerInternal;
import com.googlecode.mobilityrpc.session.MobilitySession;
import com.googlecode.mobilityrpc.session.impl.MobilitySessionInternal;
import com.googlecode.mobilityrpc.session.impl.MobilitySessionImpl;
import com.googlecode.mobilityrpc.protocol.converters.MasterMessageConverter;
import com.googlecode.mobilityrpc.protocol.processors.DeserializedMessageProcessor;
import com.googlecode.mobilityrpc.protocol.processors.DeserializedMessageProcessorRegistry;
import com.googlecode.mobilityrpc.network.ConnectionManager;
import com.googlecode.mobilityrpc.network.impl.ConnectionManagerImpl;
import com.googlecode.mobilityrpc.protocol.converters.MessageConverter;
import com.googlecode.mobilityrpc.protocol.converters.MessageConverterRegistry;
import com.googlecode.mobilityrpc.protocol.converters.MessageTypeRegistry;
import com.googlecode.mobilityrpc.protocol.pojo.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class MobilityControllerImpl implements MobilityControllerInternal {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final ConnectionManagerInternal connectionManager;

    private final ExecutorService messageProcessorService = Executors.newCachedThreadPool();

    private final MessageTypeRegistry messageTypeToClassRegistry = new MessageTypeRegistry();
    private final MessageConverterRegistry messageConverterRegistry = new MessageConverterRegistry();
    private final DeserializedMessageProcessorRegistry deserializedMessageProcessorRegistry = new DeserializedMessageProcessorRegistry();
    private final MasterMessageConverter masterMessageConverter = new MasterMessageConverter();

    private final ConcurrentMap<UUID, MobilitySessionInternal> sessionRegistry = new ConcurrentHashMap<UUID, MobilitySessionInternal>();


    public MobilityControllerImpl() {
        this.connectionManager = new ConnectionManagerImpl(this);
        connectionManager.init();
    }

    @Override
    public void receiveIncomingMessage(ConnectionId connectionId, byte[] message) {
        messageProcessorService.submit(new MessageProcessorTask(connectionId, message));
    }

    @Override
    public void sendOutgoingMessage(ConnectionId identifier, Object message) {
        // TODO: offload serialization to the queue/background thread?..
        byte[] messageDataInEnvelope = masterMessageConverter.convertToProtobuf(message);
        connectionManager.getConnection(identifier).enqueueOutgoingMessage(messageDataInEnvelope);
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Calls {@code destroy()} on the connection controller, which will unbind all connection listeners, disconnect
     * all open connections and shut down the threads which were managing connections.
     */
    @Override
    public void destroy() {
        connectionManager.destroy();
        messageProcessorService.shutdown();
        sessionRegistry.clear();
    }

    class MessageProcessorTask implements Runnable {

        private final ConnectionId connectionId;
        private final byte[] messageData;

        MessageProcessorTask(ConnectionId connectionId, byte[] messageData) {
            this.connectionId = connectionId;
            this.messageData = messageData;
        }

        @Override
        public void run() {
            processMessage(connectionId, messageData);
        }

        public <T> void processMessage(ConnectionId connectionId, byte[] messageData) {
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Processing incoming message: " + messageData.length + " bytes from " + connectionId);
                }
                Envelope envelope = messageConverterRegistry.getConverter(Envelope.class).convertFromProtobuf(messageData);

                @SuppressWarnings({"unchecked"})
                final Class<T> messageClass = (Class<T>) messageTypeToClassRegistry.getMessageClass(envelope.getMessageType());

                MessageConverter<T> messageConverter = messageConverterRegistry.getConverter(messageClass);
                DeserializedMessageProcessor<T> deserializedMessageProcessor = deserializedMessageProcessorRegistry.getProcessor(messageClass);
                T message = messageConverter.convertFromProtobuf(envelope.getMessage());

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Received message and submitting for processing, " + messageData.length + " bytes from " + connectionId + ": " + message);
                }
                deserializedMessageProcessor.process(MobilityControllerImpl.this, connectionManager, connectionId, message);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, "Failed to process incoming message: " + messageData.length + " bytes from " + connectionId, e);
            }
        }

    }



    @Override
    public MobilitySession getSession(UUID sessionId) {
        return getMessageHandlingSession(sessionId);
    }

    @Override
    public void removeSession(UUID sessionId) {
        MobilitySession session = sessionRegistry.get(sessionId);
        if (session == null) {
            return;
        }
        sessionRegistry.remove(sessionId);
    }

    @Override
    public MobilitySessionInternal getMessageHandlingSession(UUID sessionId) {
        // Retrieve existing session with the specified session id...
        MobilitySessionInternal session = sessionRegistry.get(sessionId);
        if (session != null) {
            // A session with this id was found, return it...
            logger.log(Level.FINER, "Found and returning existing session: {0}", session);
            return session;
        }

        // No session with the id was found.

        // Create a new session with the same specified id...
        MobilitySessionInternal newSession = new MobilitySessionImpl(sessionId, this);

        // Try to add the new session to the session registry atomically...
        // If we succeed in adding to the registry, existingSession will be initialised to null.
        // If we fail to add to the registry, another thread will have succeeded just before this thread,
        // and existingSession will be initialised to the session added by the other thread.
        MobilitySessionInternal existingSession = sessionRegistry.putIfAbsent(sessionId, newSession);

        if (existingSession == null) {
            logger.log(Level.FINER, "No existing session found, created registered and returning now a new session: {0}", newSession);
            return newSession;
        }
        else {
            logger.log(Level.FINER, "No existing session found, however another thread won race with this thread to create one, returning session recently created by another thread: {0}", existingSession);
            return existingSession;
        }
    }
}
