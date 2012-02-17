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
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.network.ConnectionController;
import com.googlecode.mobilityrpc.protocol.pojo.Pong;
import com.googlecode.mobilityrpc.protocol.processors.DeserializedMessageProcessor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class PongMessageProcessor implements DeserializedMessageProcessor<Pong> {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void process(MessageHandlingExecutionCoordinator executionCoordinator, ConnectionController connectionController, ConnectionIdentifier connectionIdentifier, Pong pong) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Received Pong message from connection '" + connectionIdentifier + "': " + pong);
        }
    }
}