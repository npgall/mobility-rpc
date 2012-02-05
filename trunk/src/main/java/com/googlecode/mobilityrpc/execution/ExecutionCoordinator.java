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
package com.googlecode.mobilityrpc.execution;

import com.googlecode.mobilityrpc.common.Destroyable;
import com.googlecode.mobilityrpc.session.Session;
import com.googlecode.mobilityrpc.network.ConnectionController;

import java.util.UUID;

/**
 * The main interface of the mobility-rpc library.
 * <p/>
 * Provides access to a {@link ConnectionController} object responsible for managing incoming and outgoing connections
 * to other instances of the library, and providing an API to the application to have the library bind/unbind from
 * certain ports/network interfaces to listen for incoming connections.
 * <p/>
 * Provides access to Sessions, which are the gateway through which the application can send objects to remote machines.
 * <p/>
 * @author Niall Gallagher
 */
public interface ExecutionCoordinator extends Destroyable {

    /**
     * @return The {@link ConnectionController} which manages connections for this execution coordinator
     */
    public ConnectionController getConnectionController();

    /**
     * Returns the existing session with the specified id, or if no such session with the id exists, (re)creates a
     * new session with the same id and adds it to the session registry.
     * <p/>
     * To create an entirely new session call: <code>ExecutionCoordinator.getSession(UUID.randomUUID())</code>
     *
     * @param sessionId The session id of the session to return
     * @return The existing session with the specified id, or a new session initialised with this same id
     */
    public Session getSession(UUID sessionId);
}
