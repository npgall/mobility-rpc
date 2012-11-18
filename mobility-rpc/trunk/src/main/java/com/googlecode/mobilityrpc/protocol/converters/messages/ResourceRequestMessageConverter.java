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
import com.googlecode.mobilityrpc.protocol.converters.components.RequestIdentifierComponentConverter;
import com.googlecode.mobilityrpc.protocol.pojo.ResourceRequest;
import com.googlecode.mobilityrpc.protocol.protobuf.MessageResourceRequest;

/**
 * @author Niall Gallagher
 */
public class ResourceRequestMessageConverter extends MessageConverter<ResourceRequest> {

    private final RequestIdentifierComponentConverter requestIdentifierConverter = new RequestIdentifierComponentConverter();

    @Override
    protected ResourceRequest fromProtobuf(byte[] bytes) throws Exception {
        MessageResourceRequest.ResourceRequest.Builder builder = MessageResourceRequest.ResourceRequest.newBuilder();
        builder.mergeFrom(bytes);
        return new ResourceRequest(
                builder.getResourceNameList(),
                requestIdentifierConverter.convertFromProtobuf(builder.getRequestIdentifier())
        );
    }

    @Override
    protected byte[] toProtobuf(ResourceRequest object) throws Exception {
        MessageResourceRequest.ResourceRequest.Builder builder = MessageResourceRequest.ResourceRequest.newBuilder();
        builder.addAllResourceName(object.getResourceNames());
        builder.setRequestIdentifier(requestIdentifierConverter.convertToProtobuf(object.getRequestIdentifier()));
        return builder.build().toByteArray();
    }
}
