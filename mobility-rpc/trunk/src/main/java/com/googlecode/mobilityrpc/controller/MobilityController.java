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
package com.googlecode.mobilityrpc.controller;

import com.googlecode.mobilityrpc.common.Destroyable;
import com.googlecode.mobilityrpc.network.ConnectionManager;
import com.googlecode.mobilityrpc.session.MobilitySession;

import java.util.UUID;

/**
 * Manages an instance of the Mobility-RPC library and provides access to its main APIs.
 * <p/>
 * Provides access to {@link MobilitySession} objects, the gateway through which the application can send objects
 * to remote machines.
 * <p/>
 * Provides access to a {@link ConnectionManager} object, allowing the application to request the library to bind/unbind
 * from ports/network interfaces to listen for incoming connections and receive objects from remote machines.
 *
 * @author Niall Gallagher
 */
public interface MobilityController extends Destroyable {

    /**
     * Returns the {@link ConnectionManager} which manages connections for this controller.
     *
     * @return The {@link ConnectionManager} which manages connections for this controller
     */
    public ConnectionManager getConnectionManager();

    /**
     * Returns the existing session with the specified id, or if no such session with the id exists, (re)creates a
     * new session with the same id and adds it to the session registry.
     *
     * @param sessionId The session id of the session to return
     * @return The existing session with the specified id, or a new session initialised with this same id
     */
    public MobilitySession getSession(UUID sessionId);

    /**
     * Creates a new session, with a newly generated UUID, and adds it to the session registry.
     *
     * @return A new session
     */
    public MobilitySession newSession();

    /**
     * Releases the given session immediately, if it is registered. If not registered, does nothing.
     * <p/>
     * Note: the preferred way to release a session is via
     * {@link com.googlecode.mobilityrpc.session.MobilitySession#release()}, which includes logic to gracefully release
     * sessions after threads which are using them have finished. This method would release sessions while they are in
     * use, which could cause errors for requests executing in those sessions.
     *
     * @param sessionId The session id to release
     */
    void releaseSession(UUID sessionId);

    /**
     * Destroys (closes/shuts down) the resources managed by the controller, closing connections, stopping threads,
     * releasing all sessions etc.
     */
    public void destroy();
}
