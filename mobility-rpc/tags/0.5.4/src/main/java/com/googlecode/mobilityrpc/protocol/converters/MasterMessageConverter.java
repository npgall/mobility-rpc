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

import com.googlecode.mobilityrpc.protocol.pojo.Envelope;

/**
 * A "master converter" which can convert any message object to/from protobuf and wrap/unwrap in an envelope
 * automatically.
 * <p/>
 * Delegates to other {@link MessageConverter} implementations depending on the type of message supplied, via
 * {@link MessageTypeRegistry} and {@link MessageConverterRegistry}.
 *
 * @author Niall Gallagher
 */
public class MasterMessageConverter extends MessageConverter<Object> {

    private final MessageTypeRegistry messageTypeRegistry = new MessageTypeRegistry();
    private final MessageConverterRegistry messageConverterRegistry = new MessageConverterRegistry();

    protected Object fromProtobuf(byte[] bytes) throws Exception {
        Envelope envelope = messageConverterRegistry.getConverter(Envelope.class).convertFromProtobuf(bytes);

        final Class<?> messageClass = messageTypeRegistry.getMessageClass(envelope.getMessageType());

        MessageConverter<?> messageConverter = messageConverterRegistry.getConverter(messageClass);
        return messageConverter.convertFromProtobuf(envelope.getMessage());
    }

    protected byte[] toProtobuf(Object object) throws Exception {
        return toProtobufGeneric(object);
    }

    // A helper for toProtobuf which works around us not knowing actual type of object to supply to
    // the generically-typed message converters...
    private <T> byte[] toProtobufGeneric(T object) throws Exception {

        @SuppressWarnings({"unchecked"})
        final Class<T> messageClass = (Class<T>) object.getClass();

        Envelope envelope = new Envelope(
                messageTypeRegistry.getMessageType(messageClass),
                messageConverterRegistry.getConverter(messageClass).convertToProtobuf(object)
        );
        return messageConverterRegistry.getConverter(Envelope.class).toProtobuf(envelope);
    }
}
