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

import com.google.protobuf.ByteString;
import com.googlecode.mobilityrpc.protocol.converters.MessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.Envelope;
import com.googlecode.mobilityrpc.protocol.protobuf.MessageEnvelope;

/**
 * @author Niall Gallagher
 */
public class EnvelopeMessageConverter extends MessageConverter<Envelope> {
    @Override
    protected Envelope fromProtobuf(byte[] bytes) throws Exception {
        MessageEnvelope.Envelope.Builder builder = MessageEnvelope.Envelope.newBuilder();
        builder.mergeFrom(bytes);
        return new Envelope(
                Envelope.MessageType.valueOf(builder.getMessageType().name()),
                builder.getMessageBytes().toByteArray()
        );
    }

    @Override
    protected byte[] toProtobuf(Envelope object) throws Exception {
        MessageEnvelope.Envelope.Builder builder = MessageEnvelope.Envelope.newBuilder();
        builder.setMessageType(MessageEnvelope.Envelope.MessageType.valueOf(object.getMessageType().name()));
        builder.setMessageBytes(ByteString.copyFrom(object.getMessage()));
        return builder.build().toByteArray();
    }
}
