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
import com.googlecode.mobilityrpc.protocol.pojo.ResourceResponse;
import com.googlecode.mobilityrpc.protocol.protobuf.MessageResourceResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niall Gallagher
 */
public class ResourceResponseMessageConverter extends MessageConverter<ResourceResponse> {

    private final RequestIdentifierComponentConverter requestIdentifierConverter = new RequestIdentifierComponentConverter();

    @Override
    protected ResourceResponse fromProtobuf(byte[] bytes) throws Exception {
        MessageResourceResponse.ResourceResponse.Builder builder = MessageResourceResponse.ResourceResponse.newBuilder();
        builder.mergeFrom(bytes);
        List<ResourceResponse.ResourceData> resourceResponses = new ArrayList<ResourceResponse.ResourceData>(builder.getResourceDataCount());
        for (int i = 0, n = builder.getResourceDataCount(); i < n; i++) {
            MessageResourceResponse.ResourceData protobufResourceData = builder.getResourceData(i);
            resourceResponses.add(
                    new ResourceResponse.ResourceData(
                            protobufResourceData.getResourceName(),
                            protobufResourceData.getResourceData().toByteArray()
                    )
            );
        }
        return new ResourceResponse(
                resourceResponses,
                requestIdentifierConverter.convertFromProtobuf(builder.getRequestIdentifier())
        );
    }

    @Override
    protected byte[] toProtobuf(ResourceResponse object) throws Exception {
        MessageResourceResponse.ResourceResponse.Builder builder = MessageResourceResponse.ResourceResponse.newBuilder();
        MessageResourceResponse.ResourceData.Builder resourceDataBuilder = MessageResourceResponse.ResourceData.newBuilder();
        for (ResourceResponse.ResourceData resourceData : object.getResourceDataResponses()) {
            resourceDataBuilder.setResourceName(resourceData.getResourceName());
            resourceDataBuilder.setResourceData(ByteString.copyFrom(resourceData.getResourceData()));
            builder.addResourceData(resourceDataBuilder);
        }
        builder.setRequestIdentifier(requestIdentifierConverter.convertToProtobuf(object.getRequestIdentifier()));
        return builder.build().toByteArray();
    }
}
