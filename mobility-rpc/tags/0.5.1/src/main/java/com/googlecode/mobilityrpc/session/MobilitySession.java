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
package com.googlecode.mobilityrpc.session;

import com.googlecode.mobilityrpc.controller.MobilityController;
import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionMode;
import com.googlecode.mobilityrpc.session.impl.SessionClassLoader;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * A gateway through which the application can send objects to remote machines.
 * <p/>
 * Sessions can be created or accessed via {@link MobilityController#getSession(java.util.UUID)}.
 * <p/>
 * Sessions provide the following methods for sending objects to remote machines:
 * <ul>
 *      <li>
 *          <b>{@code void execute(ConnectionId, Runnable)}</b> or <br/>
 *          <b>{@code void execute(ConnectionId, ExecutionMode, Runnable)}</b>
 *          <ul>
 *              <li>Transfers the given <code>Runnable</code> object, and any objects it references, to the given
 *              remote machine, and executes it (calls the {@link Runnable#run()} method) on the remote machine</li>
 *              <li>The {@code run()} method can call methods on on any referenced objects sent with it,
 *              or any methods in the remote application</li>
 *              <li>This is useful for invoking methods on remote machines which have {@code void} return types
 *              or where the local application is not interested in returning any data from the remote machine</li>
 *          </ul>
 *     </li>
 *     <li>
 *          <b>{@code T execute(ConnectionId, Callable&lt;T&gt;)}</b> or<br/>
 *          <b>{@code T execute(ConnectionId, ExecutionMode, Callable&lt;T&gt;)}</b>
 *          <ul>
 *              <li>Transfers the given <code>Callable</code> object, and any objects it references, to the given
 *              remote machine, and executes it (calls the {@link Callable#call()} method) on the remote machine</li>
 *              <li>The {@code call()} method can in turn call methods on on any referenced objects sent with it,
 *              or any methods in the remote application</li>
 *              <li>The {@code call()} method returns an object, and the local application can implement {@code call} to
 *              fetch an object from the remote machine. Alternatively, it could implement this to return the same
 *              Callable (the Boomerang pattern), or a referenced object which was sent alongside the Callable back
 *              again to local application</li>
 *              <li>The execute method will return the object fetched from the remote machine to the local
 *              application</li>
 *              <li>This is useful for invoking methods on remote machines which return some objects or data,
 *              and transferring those objects or data to the local application, or sending an independent object which
 *              gathers data itself and returns it back to the local application</li>
 *          </ul>
 *     </li>
 * </ul>
 * <p/>
 * The methods above take the following arguments:
 * <ul>
 *     <li>
 *          {@link ConnectionId}
 *          <ul>
 *              <li>The address and port of the remote machine to which the object should be sent</li>
 *          </ul>
 *     </li>
 *     <li>
 *          {@link ExecutionMode}
 *          <ul>
 *              <li>Specifies whether the operation should be performed synchronously or asynchronously,
 *                  that is modes {@code RETURN_RESPONSE} and {@code FIRE_AND_FORGET}, respectively
 *                  <ul>
 *                      <li>When {@link ExecutionMode#RETURN_RESPONSE} is specified
 *                          <ul>
 *                              <li>The {@code execute} method on the local machine will block (wait) until the entire
 *                              operation has been performed on the remote machine ({@code run()} or
 *                              {@code call()} invoked), and the remote machine has returned a confirmation of the
 *                              outcome of the operation to the local machine</li>
 *                          </ul>
 *                      </li>
 *                      <li>When {@link ExecutionMode#FIRE_AND_FORGET} is specified
 *                          <ul>
 *                              <li>The {@code execute} method on the local machine will queue the object for sending
 *                              to the remote machine and will then return immediately</li>
 *                              <li>A best-effort attempt will be made to execute the object on the remote machine</li>
 *                              <li>The remote machine will not send any response message</li>
 *                          </ul>
 *                      </li>
 *                  </ul>
 *              </li>
 *          </ul>
 *     </li>
 * </ul>
 *
 * @author Niall Gallagher
 */
public interface MobilitySession {

    /**
     * Returns the UUID of this session.
     * 
     * @return the UUID of this session
     */
    UUID getSessionId();

    /**
     * Transfers the given <code>Runnable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Runnable#run()} method) on the remote machine.
     * <p/>
     * This is a convenience method for calling {@link #execute(com.googlecode.mobilityrpc.network.ConnectionId, ExecutionMode, Runnable)}
     * with {@link ExecutionMode#RETURN_RESPONSE}.
     *
     * @param connectionId The address/port of the remote machine
     * @param runnable The object to send and execute on the remote machine
     */
    void execute(ConnectionId connectionId, Runnable runnable);

    /**
     * Transfers the given <code>Runnable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Runnable#run()} method) on the remote machine.
     *
     * @param connectionId The address/port of the remote machine
     * @param executionMode Either of the following, see: {@link ExecutionMode#RETURN_RESPONSE} or
     * {@link ExecutionMode#FIRE_AND_FORGET}
     * @param runnable The object to send to the remote machine
     */
    void execute(ConnectionId connectionId, ExecutionMode executionMode, Runnable runnable);

    /**
     * Transfers the given <code>Callable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Callable#call()} method) on the remote machine. Transfers the object returned
     * by the <code>call</code> method on the remote machine, and any objects it references, back to the local
     * application.
     * <p/>
     * This is a convenience method for calling {@link #execute(com.googlecode.mobilityrpc.network.ConnectionId, ExecutionMode, Runnable)}
     * with {@link ExecutionMode#RETURN_RESPONSE}.
     *
     * @param connectionId The address/port of the remote machine
     * @param callable The object to send to the remote machine
     * @return The object returned by the {@link Callable#call()} method on the remote machine (transferred back to
     * this machine)
     */
    <T> T execute(ConnectionId connectionId, Callable<T> callable);
    /**
     * Transfers the given <code>Callable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Callable#call()} method) on the remote machine. Transfers the object returned
     * by the <code>call</code> method on the remote machine, and any objects it references, back to the local
     * application.
     *
     * @param connectionId The address/port of the remote machine
     * @param executionMode Either of the following, see: {@link ExecutionMode#RETURN_RESPONSE} or
     * {@link ExecutionMode#FIRE_AND_FORGET}
     * @param callable The object to send to the remote machine
     * @return The object returned by the {@link Callable#call()} method on the remote machine (transferred back to
     * this machine)
     */
    <T> T execute(ConnectionId connectionId, ExecutionMode executionMode, Callable<T> callable);

    /**
     * Returns the class loader associated with this session.
     *
     * @return The class loader associated with this session
     */
    SessionClassLoader getSessionClassLoader();

    /**
     * Returns the mobility controller which manages this session.
     *
     * @return The mobility controller which manages this session
     */
    MobilityController getMobilityController();

    /**
     * Removes this session from the mobility controller.
     *<p/>
     * All references to the session and its class loader will be released by the library. This means that, unless some
     * application code has stored a reference to the session somewhere, the session and all of the classes it has
     * loaded will be garbage collected. Also note that this means that any data stored in static fields in those
     * classes will also be garbage collected.
     * <p/>
     * <b>Deferred Session Release</b><br/>
     * Note that if this is called on a remote machine by an object sent to that machine, the remote machine will
     * defer releasing the session until all threads concurrently processing requests in that session have finished.
     * <p/>
     * This mechanism allows mobile code to effectively schedule the session in which it is executing, on what it views
     * as the local machine, to be released after it has finished executing on that machine.
     * <p/>
     * If this method is called by a thread not managed by the library (e.g. from the host application), the method will
     * check if remote threads are executing in the session, and either schedule the session to be released when those
     * threads finish, or release it immediately if the session is actually not in use.
     * <p/>
     * See also {@link com.googlecode.mobilityrpc.controller.MobilityController#releaseSession(java.util.UUID)},
     * which bypasses this safeguard, and allows sessions to be released immediately.
     */
    void release();
}
