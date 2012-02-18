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
package com.googlecode.mobilityrpc.protocol.processors.impl;

import com.googlecode.mobilityrpc.controller.impl.MobilityControllerInternal;
import com.googlecode.mobilityrpc.network.Connection;
import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.network.ConnectionManager;
import com.googlecode.mobilityrpc.protocol.converters.messages.EnvelopeMessageConverter;
import com.googlecode.mobilityrpc.protocol.converters.messages.PongMessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.Envelope;
import com.googlecode.mobilityrpc.protocol.pojo.Ping;
import com.googlecode.mobilityrpc.protocol.pojo.Pong;
import com.googlecode.mobilityrpc.protocol.processors.DeserializedMessageProcessor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class PingMessageProcessor implements DeserializedMessageProcessor<Ping> {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final EnvelopeMessageConverter envelopeMessageConverter = new EnvelopeMessageConverter();
    private final PongMessageConverter pongMessageConverter = new PongMessageConverter();


    @Override
    public void process(MobilityControllerInternal mobilityController, ConnectionManager connectionManager, ConnectionId connectionId, Ping ping) {
        // Send a reply Pong message...
        Pong pong = new Pong(ping.getRequestId(), "PONG! " + ping.getMessage());

        byte[] pongMessage = pongMessageConverter.convertToProtobuf(pong);
        byte[] replyEnvelope = envelopeMessageConverter.convertToProtobuf(new Envelope(
                Envelope.MessageType.PONG,
                pongMessage
        ));
        Connection connection = connectionManager.getConnection(connectionId);
        connection.enqueueOutgoingMessage(replyEnvelope);
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Received and replied to Ping message from connection '" + connectionId + "': " + ping);
        }
    }
}
