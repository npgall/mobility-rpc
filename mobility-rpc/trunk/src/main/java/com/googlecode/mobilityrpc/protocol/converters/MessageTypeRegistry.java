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
package com.googlecode.mobilityrpc.protocol.converters;

import com.googlecode.mobilityrpc.protocol.pojo.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Given a message type, returns the class of its Java (POJO) representation, and vice-versa.
 *
 * @author Niall Gallagher
 */
public class MessageTypeRegistry {

    public MessageTypeRegistry() {
        register(Envelope.MessageType.EXECUTION_REQUEST, ExecutionRequest.class);
        register(Envelope.MessageType.EXECUTION_RESPONSE, ExecutionResponse.class);
        register(Envelope.MessageType.BYTE_CODE_REQUEST, ByteCodeRequest.class);
        register(Envelope.MessageType.BYTE_CODE_RESPONSE, ByteCodeResponse.class);
        register(Envelope.MessageType.PING, Ping.class);
        register(Envelope.MessageType.PONG, Pong.class);
    }

    private final Map<Envelope.MessageType, Class<?>> messageTypeToClass = new HashMap<Envelope.MessageType, Class<?>>();
    private final Map<Class<?>, Envelope.MessageType> classToMessageType = new HashMap<Class<?>, Envelope.MessageType>();

    private void register(Envelope.MessageType messageType, Class<?> messageClass) {
        messageTypeToClass.put(messageType, messageClass);
        classToMessageType.put(messageClass, messageType);
    }

    public Class<?> getMessageClass(Envelope.MessageType messageType) {
        @SuppressWarnings({"unchecked"})
        Class<?> result = messageTypeToClass.get(messageType);
        if (result == null) {
            throw new IllegalStateException("No message class for message type: " + messageType);
        }
        return result;
    }

    public Envelope.MessageType getMessageType(Class<?> messageClass) {
        Envelope.MessageType result = classToMessageType.get(messageClass);
        if (result == null) {
            throw new IllegalStateException("No message type for message class: " + messageClass);
        }
        return result;
    }
}
