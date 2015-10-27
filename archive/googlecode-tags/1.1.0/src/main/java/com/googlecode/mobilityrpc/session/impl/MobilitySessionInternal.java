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
package com.googlecode.mobilityrpc.session.impl;

import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionRequest;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionResponse;
import com.googlecode.mobilityrpc.session.MobilitySession;

/**
 * Internal interface used by the library, extends the public {@link MobilitySession} interface with methods for
 * processing incoming requests, as required by the library.
 *
 * @author Niall Gallagher
 */
public interface MobilitySessionInternal extends MobilitySession {


    /**
     * Called when we receive an incoming {@link ExecutionRequest} object from a remote machine addressed to this
     * session.
     * <p/>
     * This method will submit this request to the session's thread pool for execution asynchronously. This method
     * will return immediately.
     *
     * @param connectionId Indicates the connection from which we received the request
     * @param executionRequest A request from a remote machine to execute a serialized object on this machine
     */
    public void receiveIncomingExecutionRequest(ConnectionId connectionId, ExecutionRequest executionRequest);

    /**
     * Called when we receive an incoming {@link ExecutionResponse} object from a remote machine addressed to this
     * session.
     * <p/>
     * This method will look up and unblock the relevant thread which is waiting for the response to arrive.
     *
     * @param executionResponse A response from a remote machine for an execution request sent by a thread on this
     * machine
     */
    public void receiveExecutionResponse(ExecutionResponse executionResponse);

}
