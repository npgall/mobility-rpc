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

import com.googlecode.mobilityrpc.execution.impl.MessageHandlingExecutionCoordinator;
import com.googlecode.mobilityrpc.session.impl.MessageHandlingSession;
import com.googlecode.mobilityrpc.network.ConnectionController;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionRequest;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import com.googlecode.mobilityrpc.protocol.processors.DeserializedMessageProcessor;

import java.util.UUID;

/**
 * @author Niall Gallagher
 */
public class ExecutionRequestMessageProcessor implements DeserializedMessageProcessor<ExecutionRequest> {

    @Override
    public void process(MessageHandlingExecutionCoordinator executionCoordinator, ConnectionController connectionController, ConnectionIdentifier connectionIdentifier, ExecutionRequest executionRequest) {
        try {
            RequestIdentifier requestIdentifier = executionRequest.getRequestIdentifier();
            UUID sessionId = requestIdentifier.getSessionId();
            MessageHandlingSession session = executionCoordinator.getMessageHandlingSession(sessionId);
            session.receiveIncomingExecutionRequest(connectionIdentifier, executionRequest);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to process execution request: " + executionRequest);
        }
    }
}
