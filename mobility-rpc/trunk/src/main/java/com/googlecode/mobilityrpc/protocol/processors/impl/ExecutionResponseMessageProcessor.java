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
package com.googlecode.mobilityrpc.protocol.processors.impl;

import com.googlecode.mobilityrpc.controller.impl.MobilityControllerInternal;
import com.googlecode.mobilityrpc.network.ConnectionManager;
import com.googlecode.mobilityrpc.session.impl.MobilitySessionInternal;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionResponse;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import com.googlecode.mobilityrpc.protocol.processors.DeserializedMessageProcessor;

import java.util.UUID;

/**
 * @author Niall Gallagher
 */
public class ExecutionResponseMessageProcessor implements DeserializedMessageProcessor<ExecutionResponse> {

    @Override
    public void process(MobilityControllerInternal mobilityController, ConnectionManager connectionManager, ConnectionIdentifier connectionIdentifier, ExecutionResponse executionResponse) {
        try {
            RequestIdentifier requestIdentifier = executionResponse.getRequestIdentifier();
            UUID sessionId = requestIdentifier.getSessionId();
            MobilitySessionInternal session = mobilityController.getMessageHandlingSession(sessionId);
            // No need to pass ConnectionIdentifier to session, since calling thread will be blocked with the same
            // ConnectionIdentifier on its stack...
            session.receiveExecutionResponse(executionResponse);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to process execution response: " + executionResponse);
        }
    }
}
