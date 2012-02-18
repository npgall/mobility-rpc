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

import com.googlecode.mobilityrpc.network.ConnectionId;

/**
 * An internal interface used by the framework - implemented by an object which can be notified when connections are
 * opened or closed.
 * <p/>
 * Note: in practice this is implemented by the {@link ConnectionManager} as a means to be notified of connection
 * events, and this interface is invoked by {@link ConnectionListener}s when incoming connections are received and by
 * {@link com.googlecode.mobilityrpc.network.Connection} objects when connections are closed.
 * <p/>
 * This interface really only exists to decouple objects which send notifications from the implementations which
 * subscribe to them, to simplify independent unit testing etc.
 *
 * @author Niall Gallagher
 */
public interface ConnectionStateListener {

    /**
     * Called when the supplied connection has been opened.
     * @param connection The connection which has been opened
     */
    public void notifyConnectionOpened(ConnectionInternal connection);

    /**
     * Called when the supplied connection has been closed.
     * @param connection The connection which has been closed
     */
    public void notifyConnectionClosed(ConnectionInternal connection);

    /**
     * @return Allows the sender of notifications to query the destination
     * to see if a specific connection is registered (typically used to check if primary connection is up)
     */
    public boolean isConnectionRegistered(ConnectionId connectionId);
}
