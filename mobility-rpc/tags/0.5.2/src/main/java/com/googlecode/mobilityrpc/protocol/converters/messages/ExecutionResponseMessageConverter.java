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
package com.googlecode.mobilityrpc.protocol.converters.messages;

import com.google.protobuf.ByteString;
import com.googlecode.mobilityrpc.protocol.converters.MessageConverter;
import com.googlecode.mobilityrpc.protocol.converters.components.RequestIdentifierComponentConverter;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionResponse;
import com.googlecode.mobilityrpc.protocol.pojo.SerializationFormat;
import com.googlecode.mobilityrpc.protocol.protobuf.ComponentSerializationFormat;
import com.googlecode.mobilityrpc.protocol.protobuf.MessageExecutionResponse;

/**
 * @author Niall Gallagher
 */
public class ExecutionResponseMessageConverter extends MessageConverter<ExecutionResponse> {

    private final RequestIdentifierComponentConverter requestIdentifierConverter = new RequestIdentifierComponentConverter();

    @Override
    protected ExecutionResponse fromProtobuf(byte[] bytes) throws Exception {
        MessageExecutionResponse.ExecutionResponse.Builder builder = MessageExecutionResponse.ExecutionResponse.newBuilder();
        builder.mergeFrom(bytes);
        return new ExecutionResponse(
                ExecutionResponse.ExecutionOutcome.valueOf(builder.getExecutionOutcome().name()),
                builder.getSerializedReturnObject().toByteArray(),
                SerializationFormat.valueOf(builder.getSerializationFormat().name()),
                requestIdentifierConverter.convertFromProtobuf(builder.getRequestIdentifier())
        );
    }

    @Override
    protected byte[] toProtobuf(ExecutionResponse object) throws Exception {
        MessageExecutionResponse.ExecutionResponse.Builder builder = MessageExecutionResponse.ExecutionResponse.newBuilder();
        builder.setSerializedReturnObject(ByteString.copyFrom(object.getSerializedReturnObject()));
        builder.setSerializationFormat(ComponentSerializationFormat.SerializationFormat.valueOf(object.getSerializationFormat().name()));
        builder.setExecutionOutcome(MessageExecutionResponse.ExecutionResponse.ExecutionOutcome.valueOf(object.getExecutionOutcome().name()));
        builder.setRequestIdentifier(
                requestIdentifierConverter.convertToProtobuf(object.getRequestIdentifier())
        );
        return builder.build().toByteArray();
    }
}
