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
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionMode;
import com.googlecode.mobilityrpc.session.impl.SessionClassLoader;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * A gateway through which the application can send objects to remote machines.
 * <p/>
 * Sessions can be created or accessed via {@link MobilityController#getSession(java.util.UUID)}.
 * <p/>
 * Sessions provide two methods for sending objects to remote machines:
 * <p/>
 * <b>{@code void execute(ConnectionIdentifier, ExecutionMode, Runnable)}</b>
 * <ul>
 *     <li>Sends an object referenced by a {@link Runnable} object, or just a Runnable object on its own</li>
 *     <li>On the remote machine, the {@code run()} method of the Runnable object will be called, which can call any
 *     method on an object sent with it, or methods in the remote application</li>
 *     <li>This is useful for invoking methods on remote machines which have {@code void} return types or where the
 *     local application is not interested in returning any data from the remote machine</li>
 * </ul>
 * <p/>
 * <b>{@code T execute(ConnectionIdentifier, ExecutionMode, Callable&lt;T&gt;)}</b>
 * <ul>
 *     <li>Sends a {@link Callable} object (which, similarly, can reference objects to be sent with it)</li>
 *     <li>On the remote machine, the {@code call()} method of the Callable object will be called, which can call any
 *     method on an object sent with it, or methods in the remote application</li>
 *     <li>The {@code call()} method returns an object and the local application can implement this to fetch an
 *     object from the remote machine. Esoterically, it could implement this to return the same Callable, or an object
 *     which was sent alongside the Callable back again to local machine (the Boomerang pattern). Regardless, the object
 *     that it returns will be transferred back to the local machine and this execute method will return it to the local
 *     application</li>
 *     <li>This is useful for invoking methods on remote machines which return some object(s) or data, where the
 *     local application is interested in returning those objects or data to the local machine</li>
 *     <li>This can also be used to send an object to a remote machine which will gather some data; the data does
 *     not need to come from one method on the remote machine, the Callable object could call several methods or
 *     read several fields and package up that data to be returned at once</li>
 * </ul>
 * <p/>
 * The methods above take the following arguments:
 * <b>{@link ConnectionIdentifier}</b><br/>
 * This is a simple object which contains the ip address (or name) and port of the remote machine to which
 * the object should be sent.
 * <p/>
 * <b>{@link ExecutionMode}</b><br/>
 * This specifies whether the operation should be performed synchronously or asynchronously, see
 * <ul>
 *     <li>
 *         {@link ExecutionMode#RETURN_RESPONSE}
 *     </li>
 *     <li>
 *         {@link ExecutionMode#FIRE_AND_FORGET}
 *     </li>
 * </ul>
 *
 * @author Niall Gallagher
 */
public interface MobilitySession {

    /**
     * @return the UUID of this session
     */
    UUID getSessionId();

    /**
     * Transfers the given <code>Runnable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Runnable#run()} method) on the remote machine.
     * <p/>
     * This is a convenience method for calling {@link #execute(ConnectionIdentifier, ExecutionMode, Runnable)}
     * with {@link ExecutionMode#RETURN_RESPONSE}.
     *
     * @param connectionIdentifier The address/port of the remote machine
     * @param runnable The object to send and execute on the remote machine
     */
    void execute(ConnectionIdentifier connectionIdentifier, Runnable runnable);

    /**
     * Transfers the given <code>Runnable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Runnable#run()} method) on the remote machine.
     *
     * @param connectionIdentifier The address/port of the remote machine
     * @param executionMode Either {@link ExecutionMode#RETURN_RESPONSE} or {@link ExecutionMode#FIRE_AND_FORGET}
     * @param runnable The object to send to the remote machine
     */
    void execute(ConnectionIdentifier connectionIdentifier, ExecutionMode executionMode, Runnable runnable);

    /**
     * Transfers the given <code>Callable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Callable#call()} method) on the remote machine. Transfers the object returned
     * by the <code>call</code> method on the remote machine, and any objects it references, back to the local machine.
     * <p/>
     * This is a convenience method for calling {@link #execute(ConnectionIdentifier, ExecutionMode, Runnable)}
     * with {@link ExecutionMode#RETURN_RESPONSE}.
     *
     * @param connectionIdentifier The address/port of the remote machine
     * @param callable The object to send to the remote machine
     * @return The object returned by the {@link Callable#call()} method on the remote machine (transferred back to
     * this machine)
     */
    <T> T execute(ConnectionIdentifier connectionIdentifier, Callable<T> callable);
    /**
     * Transfers the given <code>Callable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Callable#call()} method) on the remote machine. Transfers the object returned
     * by the <code>call</code> method on the remote machine, and any objects it references, back to the local machine.
     *
     * @param connectionIdentifier The address/port of the remote machine
     * @param executionMode Either {@link ExecutionMode#RETURN_RESPONSE} or {@link ExecutionMode#FIRE_AND_FORGET}
     * @param callable The object to send to the remote machine
     * @return The object returned by the {@link Callable#call()} method on the remote machine (transferred back to
     * this machine)
     */
    <T> T execute(ConnectionIdentifier connectionIdentifier, ExecutionMode executionMode, Callable<T> callable);

    /**
     * @return The class loader associated with this session
     */
    SessionClassLoader getSessionClassLoader();

    /**
     * @return The mobility controller which manages this session
     */
    MobilityController getMobilityController();

    /**
     * Removes this session from the mobility controller.
     *<p/>
     * All references to the session and its class loader will be released by the library. This means that, unless some
     * application code has stored a reference to the session somewhere, the session and all of the classes it has
     * loaded will be garbage collected.
     */
    void release();
}
