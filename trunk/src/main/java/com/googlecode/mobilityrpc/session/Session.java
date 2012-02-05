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

import com.googlecode.mobilityrpc.protocol.pojo.ExecutionMode;
import com.googlecode.mobilityrpc.session.impl.SessionClassLoader;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.execution.ExecutionCoordinator;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * A gateway through which the application can send objects to remote machines.
 * <p/>
 * Sessions can be created or accessed via {@link ExecutionCoordinator#getSession(java.util.UUID)}.
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
 * This specifies whether the operation should be performed synchronously or asynchronously, that is modes
 * {@code RETURN_RESPONSE} and {@code FIRE_AND_FORGET}, respectively.
 * <ul>
 *     <li>When {@code RETURN_RESPONSE} is specified
 *         <ul>
 *             <li>The {@code execute} method on the local machine will block (wait) until the entire operation has been
 *             performed on the remote machine ({@code run()} or {@code call()} invoked), and the remote machine has
 *             returned a confirmation of the outcome of the operation to the local machine</li>
 *             <li>In the case of {@link Runnable} objects, the {@code run()} method does not return anything, but the
 *             method could still throw an exception on the remote machine. So in this mode the remote machine will
 *             return a success message if no exception occurred, and will return the exception if an exception
 *             occurred</li>
 *             <li>In the case of {@link Callable} objects, the {@code call()} method returns an object, but the
 *             method could alternatively throw an exception on the remote machine, and so similarly, the remote machine
 *             will return the object returned by the {@code call()} method, or will return the exception if an
 *             exception occurred</li>
 *             <li>If an exception occurs on the remote machine, it will be returned to the local machine and the
 *             the {@code execute} method on the local machine will re-throw it</li>
 *         </ul>
 *     </li>
 *     <li>When {@code FIRE_AND_FORGET} is specified
 *         <ul>
 *             <li>The {@code execute} method on the local machine will queue the object for sending to the remote
 *             machine and will then return immediately</li>
 *             <li>In the case of {@link Runnable} objects, a best-effort attempt will be made to transfer the object
 *             and invoke {@code run()} on the remote machine, but if the operation fails at any point the local
 *             application will never be informed</li>
 *             <li>In the case of {@link Callable} objects, a best-effort attempt will similarly be made, and the
 *             {@code execute} method on the local machine will return {@code null} to the local application regardless
 *             of what the {@code call()} method actually returns on the remote machine</li>
 *             <li>The local machine will inform the remote machine that no response is required, and so the remote
 *             machine will never send one, avoiding a network round-trip</li>
 *             <li>This can be useful in asynchronous applications or where the relevant operation is non-critical or
 *             long-running</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author Niall Gallagher
 */
public interface Session {

    UUID getSessionId();

    void execute(ConnectionIdentifier connectionIdentifier, ExecutionMode executionMode, Runnable runnable);

    <T> T execute(ConnectionIdentifier connectionIdentifier, ExecutionMode executionMode, Callable<T> callable);

    SessionClassLoader getSessionClassLoader();
}
