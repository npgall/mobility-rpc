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
package com.googlecode.mobilityrpc.protocol.pojo;

/**
 * @author Niall Gallagher
 */
public class Envelope {
    public enum  MessageType {
        EXECUTION_REQUEST,
        EXECUTION_RESPONSE,
        BYTE_CODE_REQUEST,
        BYTE_CODE_RESPONSE,
        PING,
        PONG
    }
    private final MessageType messageType;
    private final byte[] message;

    public Envelope(MessageType messageType, byte[] message) {
        this.messageType = messageType;
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public byte[] getMessage() {
        return message;
    }

    /**
     * @throws UnsupportedOperationException always, as this object is not intended to be compared for equality
     * or used as a key in a hash map.
     */
    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * @throws UnsupportedOperationException always, as this object is not intended to be compared for equality
     * or used as a key in a hash map.
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString() {
        return "Envelope{" +
                "messageType=" + messageType +
                ", message=" + message.length + " bytes" +
                '}';
    }
}
