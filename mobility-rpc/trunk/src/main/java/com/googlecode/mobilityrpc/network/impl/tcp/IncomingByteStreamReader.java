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
import com.googlecode.mobilityrpc.network.impl.IncomingMessageHandler;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Continuously extracts individual byte[] messages from an input stream, where each message
 * is preceded by 4 bytes which indicate the length of the message to follow.
 * <p/>
 * This reader with read continuously until {@link #shutdown()} is called, or an error occurs.
 * <p/>
 * The reader will submit each message extracted to the supplied {@link com.googlecode.mobilityrpc.network.impl.IncomingMessageHandler}.
 * <p/>
 * If an error occurs, the reader will notify the supplied {@link com.googlecode.mobilityrpc.network.impl.ConnectionErrorHandler} and then
 * will close the input stream and terminate its thread.
 *
 * @author Niall Gallagher
 */
public class IncomingByteStreamReader extends Thread {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final ConnectionId connectionId;
    private final InputStream inputStream;
    private final IncomingMessageHandler incomingMessageHandler;
    private final ConnectionErrorHandler connectionErrorHandler;

    private volatile boolean shutdown = false;

    /**
     * @param connectionId Identifies the connection to which the stream belongs
     * @param inputStream An input stream from which the reader will read messages
     * @param incomingMessageHandler An object to which the reader will supply messages extracted from the stream
     * @param connectionErrorHandler An object which the reader will notify when any exceptions occur
     */
    public IncomingByteStreamReader(ConnectionId connectionId, InputStream inputStream, IncomingMessageHandler incomingMessageHandler, ConnectionErrorHandler connectionErrorHandler) {
        this.connectionId = connectionId;
        this.connectionErrorHandler = connectionErrorHandler;
        this.inputStream = new BufferedInputStream(inputStream, 16384);
        this.incomingMessageHandler = incomingMessageHandler;
        this.setName("IncomingByteStreamReader for " + connectionId);
    }

    @Override
    public void run() {
        logger.log(Level.FINER, "IncomingByteStreamReader started for {0}", connectionId);
        while (!shutdown) {
            try {
                logger.log(Level.FINER, "Waiting for incoming messages for {0}", connectionId);
                byte[] messageSizeHeader = readBytesFromStream(inputStream, 4);
                int nextMessageSize = byteArrayToInt(messageSizeHeader);
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Receiving incoming message: " + nextMessageSize + " bytes from " + connectionId);
                }
                byte[] messageBytes = readBytesFromStream(inputStream, nextMessageSize);
                incomingMessageHandler.receiveIncomingMessage(connectionId, messageBytes);
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Received and submitted for processing incoming message: " + nextMessageSize + " bytes from " + connectionId);
                }
            }
            catch (Exception e) {
                if (!shutdown) {
                    if (e instanceof StreamClosedException) {
                        connectionErrorHandler.handle(new StreamClosedException("The connection was closed by the remote side on " + connectionId, e));
                    }
                    else {
                        connectionErrorHandler.handle(
                                new IllegalStateException("Failed to receive incoming message from " + connectionId, e)

                        );
                    }
                }
            }
        }
        this.shutdown = true;
        IOUtil.closeQuietly(inputStream);
        logger.log(Level.FINER, "IncomingByteStreamReader stopped for {0}", connectionId);
    }

    /**
     * Reads a specified number of bytes from an input stream. This method allocates a byte array of the specified
     * size up front, then reads from the stream into this byte array without allocating any additional buffers, in
     * as few round-trips to the read method of the stream as possible.
     *
     * @param is An input stream
     * @param numBytesToRead The number of bytes to read from the stream
     * @return A byte array containing the specified amount of data read from the stream
     * @throws StreamClosedException If the stream is closed (i.e. EOF is detected) while reading
     * @throws IllegalStateException If any other error occurs
     */
    static byte[] readBytesFromStream(InputStream is, int numBytesToRead) {
        try {
            byte[] bytes = new byte[numBytesToRead];

            int bytesReadEachIteration, bytesReadTotal = 0;
            while (bytesReadTotal < numBytesToRead && (bytesReadEachIteration = is.read(bytes, bytesReadTotal, numBytesToRead - bytesReadTotal)) != -1) {
                bytesReadTotal += bytesReadEachIteration;
            }
            if (bytesReadTotal < numBytesToRead) {
                throw new StreamClosedException("Stream was closed explicitly by remote side, while reading byte " + bytesReadTotal + " of " + numBytesToRead);
            }
            return bytes;
        }
        catch (StreamClosedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to read " + numBytesToRead + " bytes from stream", e);
        }
    }

    static int byteArrayToInt(byte[] bytes) {
        return (bytes[0] << 24)
                + ((bytes[1] & 0xFF) << 16)
                + ((bytes[2] & 0xFF) << 8)
                + (bytes[3] & 0xFF);
    }


    public void shutdown() {
        this.shutdown = true;
        this.interrupt();
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
