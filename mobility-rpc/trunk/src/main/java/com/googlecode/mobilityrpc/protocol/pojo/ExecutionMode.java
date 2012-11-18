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
package com.googlecode.mobilityrpc.protocol.pojo;

/**
 * @author Niall Gallagher
 */
public enum ExecutionMode {
    /**
     * A mode of execution in which the local application will wait for the remote machine to return a response. 
     * <ul>
     *     <li>The {@code execute} method on the local machine will block (wait) until the entire operation has been
     *     performed on the remote machine ({@code run()} or {@code call()} invoked), and the remote machine has
     *     returned a confirmation of the outcome of the operation to the local machine</li>
     *     <li>In the case of {@link Runnable} objects, the {@code run()} method does not return anything, but the
     *     method could still throw an exception on the remote machine. So in this mode the remote machine will
     *     return a success message if no exception occurred, and will return the exception if an exception
     *     occurred</li>
     *     <li>In the case of {@link java.util.concurrent.Callable} objects, the {@code call()} method returns an object, but the
     *     method could alternatively throw an exception on the remote machine, and so similarly, the remote machine
     *     will return the object returned by the {@code call()} method, or will return the exception if an
     *     exception occurred</li>
     *     <li>If an exception occurs on the remote machine, it will be returned to the local machine and the
     *     the {@code execute} method on the local machine will re-throw it</li>
     * </ul>
     */
    RETURN_RESPONSE,

    /**
     * A mode of execution in which the local application will not wait for the remote machine to return a response,
     * and the remote machine will not send a response.
     * <ul>
     *     <li>The {@code execute} method on the local machine will queue the object for sending to the remote
     *     machine and will then return immediately</li>
     *     <li>In the case of {@link Runnable} objects, a best-effort attempt will be made to transfer the object
     *     and invoke {@code run()} on the remote machine, but if the operation fails at any point the local
     *     application will never be informed</li>
     *     <li>In the case of {@link java.util.concurrent.Callable} objects, a best-effort attempt will similarly be made, and the
     *     {@code execute} method on the local machine will return {@code null} to the local application regardless
     *     of what the {@code call()} method actually returns on the remote machine</li>
     *     <li>The local machine will inform the remote machine that no response is required, and so the remote
     *     machine will never send one, avoiding a network round-trip</li>
     *     <li>This can be useful in asynchronous applications or where the relevant operation is non-critical or
     *     long-running</li>
     * </ul>
     * */
    FIRE_AND_FORGET
}
