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
import com.googlecode.mobilityrpc.protocol.pojo.ByteCodeResponse;
import com.googlecode.mobilityrpc.protocol.protobuf.MessageByteCodeResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niall Gallagher
 */
public class ByteCodeResponseMessageConverter extends MessageConverter<ByteCodeResponse> {

    private final RequestIdentifierComponentConverter requestIdentifierConverter = new RequestIdentifierComponentConverter();

    @Override
    protected ByteCodeResponse fromProtobuf(byte[] bytes) throws Exception {
        MessageByteCodeResponse.ByteCodeResponse.Builder builder = MessageByteCodeResponse.ByteCodeResponse.newBuilder();
        builder.mergeFrom(bytes);
        List<ByteCodeResponse.ClassData> byteCodeResponses = new ArrayList<ByteCodeResponse.ClassData>(builder.getClassDataCount());
        for (int i = 0, n = builder.getClassDataCount(); i < n; i++) {
            MessageByteCodeResponse.ClassData protobufClassData = builder.getClassData(i);
            byteCodeResponses.add(
                    new ByteCodeResponse.ClassData(
                            protobufClassData.getClassName(),
                            protobufClassData.getByteCode().toByteArray()
                    )
            );
        }
        return new ByteCodeResponse(
                byteCodeResponses,
                requestIdentifierConverter.convertFromProtobuf(builder.getRequestIdentifier())
        );
    }

    @Override
    protected byte[] toProtobuf(ByteCodeResponse object) throws Exception {
        MessageByteCodeResponse.ByteCodeResponse.Builder builder = MessageByteCodeResponse.ByteCodeResponse.newBuilder();
        MessageByteCodeResponse.ClassData.Builder classDataBuilder = MessageByteCodeResponse.ClassData.newBuilder();
        for (ByteCodeResponse.ClassData classData : object.getByteCodeResponses()) {
            classDataBuilder.setClassName(classData.getClassName());
            classDataBuilder.setByteCode(ByteString.copyFrom(classData.getByteCode()));
            builder.addClassData(classDataBuilder);
        }
        builder.setRequestIdentifier(requestIdentifierConverter.convertToProtobuf(object.getRequestIdentifier()));
        return builder.build().toByteArray();
    }
}
