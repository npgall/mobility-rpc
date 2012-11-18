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
package com.googlecode.mobilityrpc.network;

/**
 * The public interface of a connection to a remote machine.
 * <p/>
 * A connection is bidirectional and duplex, having an incoming stream of messages and outgoing stream of messages both
 * of which operate independently. A message can be sent in either direction at any time.
 * <p/>
 * The incoming stream provides messages sent to the local machine by the remote machine. The implementation of the
 * connection will typically be initialized with an object or queue to which it will pass incoming messages.
 * <p/>
 * The connection has an outgoing message queue. The connection will wait for messages to be added to the queue, and
 * when the queue is non-empty it will read from the head of the queue and send messages via the outgoing stream.
 * <p/>
 * Note that when implemented on top of the TCP protocol, that protocol is byte-stream-oriented. As such the connection
 * implementation will have a means to delimit messages sent sequentially over the streams, typically by sending a
 * message length header followed by the actual message.
 *
 * @author Niall Gallagher
 */
public interface Connection {

    /**
     * @return An object which identifies the endpoint (socket and port) of the connection.
     */
    public ConnectionId getConnectionId();

    /**
     * Submits the specified message to the connection's outgoing message queue, such that it will be
     * sent to the machine at the other end of the connection for processing.
     *
     * @param message A java object representation of a protocol message
     */
    public void enqueueOutgoingMessage(byte[] message);

}
