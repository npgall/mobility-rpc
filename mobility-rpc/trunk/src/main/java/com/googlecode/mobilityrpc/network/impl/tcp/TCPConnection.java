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
package com.googlecode.mobilityrpc.network.impl.tcp;

import com.googlecode.mobilityrpc.network.*;
import com.googlecode.mobilityrpc.network.impl.*;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class TCPConnection implements ConnectionInternal {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Socket socket;
    private final ConnectionIdentifier connectionIdentifier;

    private final BlockingQueue<byte[]> outgoingMessageQueue = new LinkedBlockingQueue<byte[]>();
    private final IncomingMessageHandler incomingMessageHandler;
    private final ConnectionStateListener connectionStateListener;

    private IncomingByteStreamReader incomingByteStreamReader = null;
    private OutgoingByteStreamWriter outgoingByteStreamWriter = null;

    /**
     *
     * @param socket The socket through which this connection communicates.
     * @param auxiliaryConnectionId If zero, indicates that this is a primary connection, otherwise this is
     * simply a number which distinguishes this connection from other "auxiliary" connections to the remote machine
     * @param incomingMessageHandler An object to which messages received on this connection should be supplied.
     * @param connectionStateListener An object which this connection should notify if the connection is closed.
     */
    public TCPConnection(Socket socket, int auxiliaryConnectionId, IncomingMessageHandler incomingMessageHandler, ConnectionStateListener connectionStateListener) {
        this.socket = socket;
        this.incomingMessageHandler = incomingMessageHandler;
        this.connectionIdentifier = new ConnectionIdentifier(
                socket.getInetAddress().getHostAddress(),
                socket.getPort(),
                auxiliaryConnectionId);
        this.connectionStateListener = connectionStateListener;
    }

    @Override
    public ConnectionIdentifier getConnectionIdentifier() {
        return connectionIdentifier;
    }

    @Override
    public void enqueueOutgoingMessage(byte[] message) {
        outgoingMessageQueue.add(message);
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Enqueued outgoing message for connection identifier '" + connectionIdentifier + "': " + message.length + " bytes");
        }
    }

    @Override
    public void init() {
        if (incomingByteStreamReader != null || outgoingByteStreamWriter != null) {
            throw new IllegalStateException("Already initialised.");
        }
        try {
            incomingByteStreamReader = new IncomingByteStreamReader(
                    connectionIdentifier,
                socket.getInputStream(),
                incomingMessageHandler,
                new ConnectionErrorHandler() {
                    @Override
                    public void handle(Exception e) {
                        if (e instanceof StreamClosedException) {
                            // Remote side closed connection in an orderly manner...
                            if (logger.isLoggable(Level.FINEST)) {
                                // FINEST level logging is enabled
                                // Log that we are closing our end of the connection and include more detail
                                // (stack trace of where we were when stream was closed)...
                                logger.log(Level.FINEST, "Stream closed explicitly by remote side, closing connection: " + connectionIdentifier, e);
                            } else {
                                // FINEST level logging is not enabled.
                                // Log at FINE level with minimal detail that remote side disconnected and that we are
                                // closing our end of the connection (this will be a common and expected occurrence)...
                                logger.log(Level.FINE, "Stream closed explicitly by remote side, closing connection (enable finest-level logging for more detail): {0}", connectionIdentifier);
                            }
                        }
                        else {
                            // Some unexpected exception occurred.
                            // Log at WARNING level details of the exception and that we will close our connection
                            // as a result...
                            logger.log(Level.WARNING, "Exception in IncomingByteStreamReader, closing connection: " + connectionIdentifier, e);
                        }
                        destroy();
                    }
                }
            );
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to initialize IncomingByteStreamReader for: " + connectionIdentifier, e);
        }
        try {
            outgoingByteStreamWriter = new OutgoingByteStreamWriter(
                    connectionIdentifier,
                socket.getOutputStream(),
                new MessageProvider<byte[]>() {
                    @Override
                    public byte[] getNextMessage() {
                        try {
                            return outgoingMessageQueue.take();
                        }
                        catch (InterruptedException e) {
                            throw new IllegalStateException("Interrupted while waiting to take message fom outgoing message queue", e);
                        }
                    }
                },
                new ConnectionErrorHandler() {
                    @Override
                    public void handle(Exception e) {
                        logger.log(Level.WARNING, "Exception in OutgoingByteStreamWriter, closing connection: " + connectionIdentifier, e);
                        destroy();
                    }
                }
            );
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to initialize OutgoingByteStreamWriter for: " + connectionIdentifier, e);
        }
        incomingByteStreamReader.start();
        outgoingByteStreamWriter.start();
        logger.log(Level.FINER, "Initialized TCP connection for: {0}", connectionIdentifier);
    }

    @Override
    public void destroy() {
        if (incomingByteStreamReader == null || outgoingByteStreamWriter == null) {
            return;
        }
        incomingByteStreamReader.shutdown();
        outgoingByteStreamWriter.shutdown();
        connectionStateListener.notifyConnectionClosed(this);
    }

}
