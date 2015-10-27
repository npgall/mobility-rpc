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

import com.googlecode.mobilityrpc.protocol.converters.messages.*;
import com.googlecode.mobilityrpc.protocol.pojo.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Given the class of a Java (POJO) object which represents a message, returns the relevant {@link MessageConverter}
 * which can convert instances of that class to/from protobuf.
 *
 * @author Niall Gallagher
 */
public class MessageConverterRegistry {

    public MessageConverterRegistry() {
        register(Envelope.class, new EnvelopeMessageConverter());
        register(ByteCodeRequest.class, new ByteCodeRequestMessageConverter());
        register(ByteCodeResponse.class, new ByteCodeResponseMessageConverter());
        register(ExecutionRequest.class, new ExecutionRequestMessageConverter());
        register(ExecutionResponse.class, new ExecutionResponseMessageConverter());
        register(Ping.class, new PingMessageConverter());
        register(Pong.class, new PongMessageConverter());
    }

    private final Map<Class<?>, MessageConverter<?>> converters = new HashMap<Class<?>, MessageConverter<?>>();

    void register(Class<?> messageClass, MessageConverter<?> messageConverter) {
        converters.put(messageClass, messageConverter);
    }
    
    public <T> MessageConverter<T> getConverter(Class<T> messageClass) {
        @SuppressWarnings({"unchecked"})
        MessageConverter<T> result = (MessageConverter<T>) converters.get(messageClass);
        if (result == null) {
            throw new IllegalStateException("No MessageConverter for message type: " + messageClass);
        }
        return result;
    }
}
