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
package com.googlecode.mobilityrpc.protocol.processors;

import com.googlecode.mobilityrpc.protocol.pojo.*;
import com.googlecode.mobilityrpc.protocol.processors.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niall Gallagher
 */
public class DeserializedMessageProcessorRegistry {

    private final Map<Class<?>, DeserializedMessageProcessor<?>> converters = new HashMap<Class<?>, DeserializedMessageProcessor<?>>() {{
        put(ExecutionRequest.class, new ExecutionRequestMessageProcessor());
        put(ExecutionResponse.class, new ExecutionResponseMessageProcessor());
        put(ResourceRequest.class, new ResourceRequestMessageProcessor());
        put(ResourceResponse.class, new ResourceResponseMessageProcessor());
        put(Ping.class, new PingMessageProcessor());
        put(Pong.class, new PongMessageProcessor());
    }};

    public <T> DeserializedMessageProcessor<T> getProcessor(Class<T> messageClass) {
        @SuppressWarnings({"unchecked"})
        DeserializedMessageProcessor<T> result = (DeserializedMessageProcessor<T>) converters.get(messageClass);
        if (result == null) {
            throw new IllegalStateException("No DeserializedMessageProcessor for message type: " + messageClass);
        }
        return result;
    }
}
