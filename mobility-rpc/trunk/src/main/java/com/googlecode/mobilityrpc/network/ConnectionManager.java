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
package com.googlecode.mobilityrpc.network;

import java.util.Collection;

/**
 * The public interface of an object which keeps track of incoming and outgoing connections to/from remote machines,
 * and which controls the opening and closing of server sockets on the local machine to receive incoming connections.
 * <p/>
 * The {@link #getConnection} method allows client code to obtain a connection to a remote machine, establishing
 * connections or reusing inbound connections as necessary.
 * <p/>
 * The {@link #bindConnectionListener} and {@link #unbindConnectionListener} methods cause the controller to start and
 * stop (respectively) {@link com.googlecode.mobilityrpc.network.impl.ConnectionListener}s which open server sockets on the local machine to listen for inbound
 * connections from remote machines on the specified ports.
 *
 * @author Niall Gallagher
 */
public interface ConnectionManager {

    /**
     * Returns a connection to the destination specified.
     * <p/>
     * If an inbound connection has previously been received from this destination, returns that connection.
     * <p/>
     * If an outbound connection has previously been established to the destination, returns that connection.
     * <p/>
     * If no connection to the destination currently exists, establishes a new outgoing connection to the destination,
     * and returns the new connection.
     * <p/>
     * The method caches any new connection it creates and will return the same connection again in future.
     * The connection itself will will un-register itself from this cache if it becomes disconnected.
     *
     * @param destinationIdentifier Identifies a remote machine (address and port) to which a connection is required
     * @return A connection to the remote machine
     */
    public Connection getConnection(ConnectionId destinationIdentifier);

    /**
     * Opens a listener on the local machine to receive inbound connections from other machines to the specified
     * address and port on the local machine.
     * <p/>
     * The address supplied could be {@code localhost} to receive connections only from other processes on the local
     * machine, or could be the local machine's IP address or host name on its network.
     *
     * @param localEndpointIdentifier Identifies an address and port on the local machine
     */
    public void bindConnectionListener(ConnectionId localEndpointIdentifier);

    /**
     * Closes a listener on the local machine which was previously opened via the
     * {@link #bindConnectionListener(ConnectionId)} method.
     * @param localEndpointIdentifier The localEndpointIdentifier previously used to bind the connection
     */
    public void unbindConnectionListener(ConnectionId localEndpointIdentifier);

    /**
     * @return The set of {@code ConnectionId}s for the currently bound listeners
     */
    public Collection<ConnectionId> getConnectionListenerIdentifiers();

    /**
     * @return The set of {@code ConnectionId}s for the currently open connections
     */
    public Collection<ConnectionId> getConnectionIds();
}
