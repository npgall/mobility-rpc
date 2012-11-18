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
package com.googlecode.mobilityrpc.network.impl;

import com.googlecode.mobilityrpc.network.ConnectionId;

/**
 * An internal interface, implemented by objects which can accept a Java object representation of a protocol
 * message, and submit it for serialization and sending to a remote machine.
 *
 * @author Niall Gallagher
 */
public interface OutgoingMessageHandler {

    /**
     * Serializes the supplied message object and submits it to the outgoing message queue of the connection indicated,
     * such that it will be sent to the machine at the other end of the connection for processing.
     * <p/>
     * The object supplied must be an instance of one of the Java protocol message representations from the
     * {@link com.googlecode.mobilityrpc.protocol.pojo} package, such that it can be serialized to its protobuf counterpart
     * representation.
     * <p/>
     * The object supplied will be serialized to protobuf format synchronously inside this method, by the thread
     * calling this method. The method will only then submit the message in protobuf format to the connection's
     * outgoing message queue, where it will be sent asynchronously.
     * <p/>
     * As such if the object supplied cannot be serialized to protobuf format, this method will throw an exception
     * to the code calling this method. This method does not block.
     *
     * @param connectionId Identifies the connection through which the message should be sent
     * @param message A java object representation of a protocol message
     */
    public void sendOutgoingMessage(ConnectionId connectionId, Object message);
}
