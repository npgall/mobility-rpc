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
package com.googlecode.mobilityrpc.protocol.processors;

import com.googlecode.mobilityrpc.controller.MobilityController;
import com.googlecode.mobilityrpc.controller.impl.MobilityControllerImpl;
import com.googlecode.mobilityrpc.network.Connection;
import com.googlecode.mobilityrpc.network.ConnectionManager;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.protocol.converters.messages.EnvelopeMessageConverter;
import com.googlecode.mobilityrpc.protocol.converters.messages.PingMessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.Envelope;
import com.googlecode.mobilityrpc.protocol.pojo.Ping;
import com.googlecode.mobilityrpc.util.LoggingUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author Niall Gallagher
 */
public class PingPongTest {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    static {
        LoggingUtil.setSingleLineLoggingFormat();
        LoggingUtil.setAppLoggingLevel(Level.FINER);
    }

    // A test which runs the listener and client in the same JVM.
    // Note this is therefore not an ideal test. You can run a listener and client in separate JVMs manually, see below.
    @Test
    public void testPingPong() {
        executor.submit(new Runnable() {
            public void run() {
                testStartListener();
            }
        });
        sleep(1);
        executor.submit(new Runnable() {
            public void run() {
                testStartClient();
            }
        });
        sleep(10);
    }

    @Test
    @Ignore // ignored to not run this automatically.
    // Run this for manual testing in its own JUnit process (right-click in IDE),
    // and simultaneously run testStartClient in a separate JUnit process...
    public void testStartListener() {
        final MobilityController mobilityController = new MobilityControllerImpl();
        final ConnectionManager connectionManager = mobilityController.getConnectionManager();

        final ConnectionIdentifier localEndpointIdentifier = new ConnectionIdentifier("127.0.0.1", 5739);
        connectionManager.bindConnectionListener(localEndpointIdentifier);
        sleep(10);
        connectionManager.unbindConnectionListener(localEndpointIdentifier);
    }

    @Test
    @Ignore // ignored to not run this automatically.
    // Run this for manual testing in its own JUnit process (right-click in IDE),
    // and simultaneously run testStartListener in a separate JUnit process...
    public void testStartClient() {
        final PingMessageConverter pingMessageConverter = new PingMessageConverter();
        final EnvelopeMessageConverter envelopeMessageConverter = new EnvelopeMessageConverter();
        
        final MobilityController mobilityController = new MobilityControllerImpl();
        final ConnectionManager connectionManager = mobilityController.getConnectionManager();

        Connection connection = connectionManager.getConnection(new ConnectionIdentifier("127.0.0.1", 5739));

        // Send a Ping message...
        Ping ping = new Ping(UUID.randomUUID(), "Hello World");

        byte[] pingMessage = pingMessageConverter.convertToProtobuf(ping);
        byte[] pingEnvelope = envelopeMessageConverter.convertToProtobuf(new Envelope(
                Envelope.MessageType.PING,
                pingMessage
        ));
        connection.enqueueOutgoingMessage(pingEnvelope);
        sleep(10);
    }

    void sleep(int numSeconds) {
        try {
            TimeUnit.SECONDS.sleep(numSeconds);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Exception while sleeping for " + numSeconds + " seconds", e);
        }
    }
}
