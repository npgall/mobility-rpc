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
package com.googlecode.mobilityrpc.protocol.converters.messages;

import com.googlecode.mobilityrpc.protocol.converters.MessageConverter;
import com.googlecode.mobilityrpc.protocol.converters.components.UuidComponentConverter;
import com.googlecode.mobilityrpc.protocol.pojo.Pong;
import com.googlecode.mobilityrpc.protocol.protobuf.MessagePong;

/**
 * @author Niall Gallagher
 */
public class PongMessageConverter extends MessageConverter<Pong> {

    private final UuidComponentConverter uuidConverter = new UuidComponentConverter();

    @Override
    protected Pong fromProtobuf(byte[] bytes) throws Exception {
        MessagePong.Pong.Builder builder = MessagePong.Pong.newBuilder();
        builder.mergeFrom(bytes);
        return new Pong(
                uuidConverter.convertFromProtobuf(builder.getRequestId()),
                builder.getMessage()
        );
    }

    @Override
    protected byte[] toProtobuf(Pong object) throws Exception {
        MessagePong.Pong.Builder builder = MessagePong.Pong.newBuilder();
        builder.setRequestId(uuidConverter.convertToProtobuf(object.getRequestId()));
        builder.setMessage(object.getMessage());
        return builder.build().toByteArray();
    }
}
