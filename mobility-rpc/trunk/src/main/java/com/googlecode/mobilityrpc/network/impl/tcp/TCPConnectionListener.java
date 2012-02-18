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

import com.googlecode.mobilityrpc.common.util.IOUtil;
import com.googlecode.mobilityrpc.network.*;
import com.googlecode.mobilityrpc.network.impl.ConnectionInternal;
import com.googlecode.mobilityrpc.network.impl.ConnectionListenerInternal;
import com.googlecode.mobilityrpc.network.impl.ConnectionStateListener;
import com.googlecode.mobilityrpc.network.impl.IncomingMessageHandler;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of ConnectionListener which uses a TCP ServerSocket.
 * <p/>
 * This implementation provides defaults for most parameters, see documentation on setter methods.
 * <p/>
 * @author Niall Gallagher
 */
public class TCPConnectionListener implements ConnectionListenerInternal {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final IncomingMessageHandler incomingMessageHandler;
    private final ConnectionStateListener connectionStateListener;
    private final ConnectionIdentifier localEndpointIdentifier;

    /**
     * Auxiliary connections are additional connections that we receive from (or establish to) a remote machine beyond
     * the first (primary) connection.
     * <p/>
     * When we accept a new connection from a remote machine and we find that we already have a primary connection
     * to/from that machine, we allocate the received connection an auxiliary connection id which is less than zero.
     * <p/>
     * Auxiliary connection ids are never communicated, they are used only within this application instance. However
     * since we encapsulate the auxiliary connection id inside the connection identifier which we create for the
     * connection and pass to upper layers, any requests we receive via this connection will be associated with this
     * connection identifier and so when returning responses, we will route responses via the same connection.
     * <p/>
     * Applications which wish to establish outgoing auxiliary connections are responsible for choosing auxiliary
     * connection ids themselves, which they supply to the constructor of {@link ConnectionIdentifier}, and those
     * auxiliary connection ids should always be greater than zero.
     * <p/>
     * We always assign auxiliary connections which we receive arbitrary auxiliary connection ids which are less than
     * zero. This means that the application instance on the side which receives an auxiliary connection, is unlikely
     * to be aware of its existence - auxiliary connections are intended for use by the originating side of the
     * connection.
     * <p/>
     * Note that if our primary connection breaks, the library attempts to reestablish it for the next message to be
     * routed via it, however while it is down we prevent applications from establishing new auxiliary connections.
     * This is to avoid an edge case where an incoming auxiliary connection could be interpreted as a primary
     * connection on the receiving side.
     * <p/>
     * We decrement this AtomicInteger to allocate (effectively arbitrary) auxiliary connection identifiers.
     */
    private final AtomicInteger auxiliaryConnectionIdProvider = new AtomicInteger();


    private Acceptor acceptor;

    // Executor service for the Acceptor,
    // initially 0 threads, scales to 1 thread,
    // shuts down threads immediately when acceptor exits...
    private final ExecutorService acceptorService = new ThreadPoolExecutor(0, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    /**
     * @param localEndpointIdentifier An object which contains parameters for the listener which will be used
     * @param incomingMessageHandler An object to which message received should be passed
     * @param connectionStateListener an object which should be notified if the connection is closed
     */
    public TCPConnectionListener(ConnectionIdentifier localEndpointIdentifier, IncomingMessageHandler incomingMessageHandler, ConnectionStateListener connectionStateListener) {
        this.localEndpointIdentifier = localEndpointIdentifier;
        this.incomingMessageHandler = incomingMessageHandler;
        this.connectionStateListener = connectionStateListener;
    }


    @Override
    public void init() {
        if (this.acceptor != null) {
            throw new IllegalStateException("Already initialized.");
        }
        try {
            InetAddress bindAddress = InetAddress.getByName(localEndpointIdentifier.getAddress());
            final ServerSocket serverSocket = new ServerSocket(localEndpointIdentifier.getPort(), 50, bindAddress);
            Acceptor acceptor = new Acceptor(serverSocket);

            acceptorService.submit(acceptor);
            this.acceptor = acceptor;
            logger.log(Level.FINE, "Initialized connection listener for local endpoint: {0}", localEndpointIdentifier);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to initialize connection listener for local endpoint: " + localEndpointIdentifier, e);
        }
    }

    @Override
    public void destroy() {
        Acceptor acceptor = this.acceptor;
        if (acceptor != null) {
            acceptor.stop();
        }
        logger.log(Level.FINE, "Destroyed connection listener for local endpoint: {0}", localEndpointIdentifier);
    }


    @Override
    public ConnectionIdentifier getConnectionIdentifier() {
        return localEndpointIdentifier;
    }

    class Acceptor implements Runnable {

        private final ServerSocket serverSocket;
        private volatile boolean stopSignalled = false;

        Acceptor(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            try {
                logger.log(Level.FINE, "Connection listener started for local endpoint: {0}", localEndpointIdentifier);
                // Loop until thread is interrupted or shutdown flag is set...
                //noinspection InfiniteLoopStatement
                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    logger.log(Level.FINER, "Waiting for connections to local endpoint: {0}", localEndpointIdentifier);
                    Socket socket = serverSocket.accept();
                    // Create a TCPConnection object to maintain this connection, and pass the ConnectionManager to
                    // it so that it can register itself when we call init() and unregister itself when the connection
                    // is closed...

                    final int auxiliaryConnectionId;
                    if (!connectionStateListener.isConnectionRegistered(new ConnectionIdentifier(socket.getInetAddress().getHostAddress(), socket.getPort(), 0))) {
                        // A primary connection is not registered,
                        // register this incoming connection as the primary connection...
                        auxiliaryConnectionId = 0;
                    }
                    else {
                        // A primary connection is already established, register this as an auxiliary connection...
                        auxiliaryConnectionId = auxiliaryConnectionIdProvider.decrementAndGet();
                    }
                    ConnectionInternal connection = new TCPConnection(socket, auxiliaryConnectionId, incomingMessageHandler, connectionStateListener);
                    if (logger.isLoggable(Level.FINER)) {
                        logger.log(Level.FINER, "Received connection on local endpoint " + localEndpointIdentifier + " from " + connection.getConnectionIdentifier());
                    }
                    // Initialise the connection, and register it with the ConnectionManager...
                    connection.init();
                    connectionStateListener.notifyConnectionOpened(connection);
                }
            }
            catch (Exception e) {
                if (stopSignalled) {
                    logger.log(Level.FINE, "ConnectionListener stopped for local endpoint: {0}", localEndpointIdentifier);
                }
                else {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "ConnectionListener stopped due to exception for local endpoint: " + localEndpointIdentifier, e);
                    }
                    stop();
                }
            }
        }

        public void stop() {
            this.stopSignalled = true;
            // Close server socket, which will cause IOException in accept() method...
            IOUtil.closeQuietly(serverSocket);
            acceptor = null;
        }
    }
}
