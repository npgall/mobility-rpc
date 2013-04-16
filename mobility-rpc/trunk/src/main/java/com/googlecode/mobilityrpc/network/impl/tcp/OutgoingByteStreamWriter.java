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
package com.googlecode.mobilityrpc.network.impl.tcp;

import com.googlecode.mobilityrpc.common.util.IOUtil;
import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.network.impl.ConnectionErrorHandler;
import com.googlecode.mobilityrpc.network.impl.MessageProvider;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class OutgoingByteStreamWriter extends Thread {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final ConnectionId connectionId;
    private final OutputStream outputStream;
    private final MessageProvider<byte[]> outgoingMessageProvider;
    private final ConnectionErrorHandler connectionErrorHandler;

    /**
     * @param connectionId Identifies the connection to which the stream belongs
     * @param outputStream An output stream to which the writer will write messages
     * @param outgoingMessageProvider An object from which the reader will obtain messages to write
     * @param connectionErrorHandler An object which the writer will notify when any exceptions occur
     */
    public OutgoingByteStreamWriter(ConnectionId connectionId, OutputStream outputStream, MessageProvider<byte[]> outgoingMessageProvider, ConnectionErrorHandler connectionErrorHandler) {
        this.connectionId = connectionId;
        this.outputStream = new BufferedOutputStream(outputStream, 16384);
        this.outgoingMessageProvider = outgoingMessageProvider;
        this.connectionErrorHandler = connectionErrorHandler;
        this.setName("OutgoingByteStreamWriter for " + connectionId);
    }

    private volatile boolean shutdown = false;

    @Override
    public void run() {
        logger.log(Level.FINER, "OutgoingByteStreamWriter started for {0}", connectionId);
        while (!shutdown) {
            try {
                logger.log(Level.FINER, "Waiting for outgoing messages for {0}", connectionId);
                byte[] nextMessage = outgoingMessageProvider.getNextMessage();
                int nextMessageSize = nextMessage.length;
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Sending outgoing message: " + nextMessageSize + " bytes to " + connectionId);
                }

                writeNextMessageSize(outputStream, nextMessageSize);
                outputStream.write(nextMessage);
                outputStream.flush();
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Sent outgoing message: " + nextMessageSize + " bytes to " + connectionId);
                }
            }
            catch (Exception e) {
                if (!shutdown) {
                    connectionErrorHandler.handle(
                            new IllegalStateException("Failed to send outgoing message to " + connectionId, e)
                    );
                }
            }
        }
        this.shutdown = true;
        IOUtil.closeQuietly(outputStream);
        logger.log(Level.FINER, "OutgoingByteStreamWriter stopped for {0}", connectionId);
    }

    void writeNextMessageSize(OutputStream outputStream, int nextMessageSize) {
        try {
            byte[] nextMessageSizeBytes = intToByteArray(nextMessageSize);
            outputStream.write(nextMessageSizeBytes);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to write 4-byte indicator of next message size", e);
        }
    }

    byte[] intToByteArray(int value) {
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    public void shutdown() {
        this.shutdown = true;
        this.interrupt();
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
