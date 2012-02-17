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
package com.googlecode.mobilityrpc.network.impl;

import com.googlecode.mobilityrpc.execution.impl.ExecutionCoordinatorImpl;
import com.googlecode.mobilityrpc.network.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class ConnectionControllerImpl implements ManagedConnectionController, ConnectionStateListener {

    private final ConcurrentMap<ConnectionIdentifier, ManagedConnection> connections = new ConcurrentHashMap<ConnectionIdentifier, ManagedConnection>();
    private final ConcurrentMap<ConnectionIdentifier, ManagedConnectionListener> incomingConnectionListeners = new ConcurrentHashMap<ConnectionIdentifier, ManagedConnectionListener>();

    private final ExecutionCoordinatorImpl executionCoordinator;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public ConnectionControllerImpl(ExecutionCoordinatorImpl executionCoordinator) {
        this.executionCoordinator = executionCoordinator;
    }

    @Override
    public Connection getConnection(ConnectionIdentifier identifier) {
        ManagedConnection connection = connections.get(identifier);
        // Double-checked lock for thread safety, to establish an outgoing connection only if necessary.
        // Note ideally we would use ConcurrentMap semantics alone without additional synchronization here,
        // but creating a new connection prior to calling putIfAbsent would be expensive in the the case the
        // connection already exists.
        if (connection == null) {
            synchronized (connections) {
                connection = connections.get(identifier);
                if (connection == null) {
                    // Create and cache new connection...
                    connection = createOutgoingConnection(identifier);
                }
            }
        }
        return connection;
    }

    ManagedConnection createOutgoingConnection(ConnectionIdentifier identifier) {
        synchronized (connections) {
            final int auxiliaryConnectionId = identifier.getAuxiliaryConnectionId();
            if (auxiliaryConnectionId < 0) {
                // Only the library itself can create auxiliary connections with ids < 0, and even then it will only
                // do so for incoming auxiliary connections.
                throw new IllegalArgumentException("Cannot establish an outgoing connection with the auxiliary connection id specified: " + identifier);
            }
            else if (auxiliaryConnectionId > 0 && !connections.containsKey(new ConnectionIdentifier(identifier.getAddress(), identifier.getPort(), 0))) {
                // We cannot create outgoing auxiliary connections while the primary connection is down.
                // See documentation in ConnectionControllerImpl#auxiliaryConnectionIdProvider...
                throw new IllegalStateException("Cannot establish an outgoing auxiliary connection, because the primary connection is down: " + identifier);
            }
            // At this point, we are about to establish either:
            // - the primary connection, or
            // - an auxiliary connection (and we know that the primary connection is up)...

            final Socket socket;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(InetAddress.getByName(identifier.getAddress()), identifier.getPort()));
            }
            catch (Exception e) {
                throw new IllegalStateException("Failed to establish outgoing connection to: " + identifier, e);
            }
            // Wrap the socket in a TCPConnection object which will manage the sockets incoming and outgoing streams...
            ManagedConnection connection = new TCPConnection(
                    socket,
                    auxiliaryConnectionId,
                    executionCoordinator,
                    this
            );
            connection.init();
            notifyConnectionOpened(connection);
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Created outgoing connection to: " + identifier);
            }
            return connection;
        }
    }

    public void notifyConnectionOpened(ManagedConnection connection) {
        ManagedConnection existing = connections.putIfAbsent(connection.getConnectionIdentifier(), connection);
        if (existing != null) {
            // Very unlikely scenario.
            // We must be dealing with a new incoming connection, where the client had previously been connected
            // to us from the same client-side port as we are now receiving this connection from.
            // The client must have closed the previous connection without us realising yet, then tried to connect
            // to us again from the same client-side port.
            // Outgoing connections from clients should use ephemeral ports, hence repeat connections from the
            // same client port should be incredibly unlikely...
            throw new IllegalStateException("Duplicate connection detected, a connection is already registered for identifier: " + connection.getConnectionIdentifier());
        }
    }

    public void notifyConnectionClosed(ManagedConnection connection) {
        connections.remove(connection.getConnectionIdentifier());
    }

    @Override
    public boolean isConnectionRegistered(ConnectionIdentifier connectionIdentifier) {
        synchronized (connections) {
            return connections.containsKey(connectionIdentifier);
        }
    }

    @Override
    public void bindConnectionListener(ConnectionIdentifier localEndpointIdentifier) {
        synchronized (incomingConnectionListeners) {
            ManagedConnectionListener newListener = new TCPConnectionListener(localEndpointIdentifier, executionCoordinator, this);
            ManagedConnectionListener existingListener = incomingConnectionListeners.putIfAbsent(localEndpointIdentifier, newListener);
            if (existingListener != null) {
                throw new IllegalStateException("A listener is already registered for connection identifier: " + localEndpointIdentifier);
            }
            try {
                newListener.init();
            }
            catch (RuntimeException e) {
                incomingConnectionListeners.remove(localEndpointIdentifier);
                throw e;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Created connection listener for local endpoint: " + localEndpointIdentifier);
            }
        }
    }

    public void unbindConnectionListener(ConnectionIdentifier localEndpointIdentifier) {
        synchronized (incomingConnectionListeners) {
            ManagedConnectionListener existing = incomingConnectionListeners.remove(localEndpointIdentifier);
            if (existing == null) {
                throw new IllegalStateException("No such listener is registered for connection identifier: " + localEndpointIdentifier);
            }
            existing.destroy();
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Destroyed connection listener for local endpoint: " + localEndpointIdentifier);
            }
        }
    }

    @Override
    public Collection<ConnectionIdentifier> getConnectionListenerIdentifiers() {
        return Collections.unmodifiableSet(incomingConnectionListeners.keySet());
    }

    public Collection<ConnectionIdentifier> getConnectionIdentifiers() {
        return Collections.unmodifiableSet(connections.keySet());
    }

    /**
     * Does nothing in the current implementation.
     */
    @Override
    public void init() {
        // No op.
    }

    @Override
    public void destroy() {
        // Close all connection listeners...
        for (ConnectionIdentifier listenerIdentifier : incomingConnectionListeners.keySet()) {
            unbindConnectionListener(listenerIdentifier);
        }
        // Close all existing connections (note: Connections will unregister themselves automatically)...
        for (ManagedConnection connection : connections.values()) {
            connection.destroy();
        }
    }
}
