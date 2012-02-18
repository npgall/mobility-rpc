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
package com.googlecode.mobilityrpc.network.impl;

import com.googlecode.mobilityrpc.network.ConnectionId;

/**
 * An internal interface, implemented by objects which can accept and process a serialized
 * protocol message (byte array) received from a remote machine.
 *
 * @author Niall Gallagher
 */
public interface IncomingMessageHandler {

    /**
     * Supplies the message to the handler for it to be processed.
     *
     * @param connectionId Identifies the connection from which the message was received
     * @param serializedMessage The message to supply
     */
    public void receiveIncomingMessage(ConnectionId connectionId, byte[] serializedMessage);
}
