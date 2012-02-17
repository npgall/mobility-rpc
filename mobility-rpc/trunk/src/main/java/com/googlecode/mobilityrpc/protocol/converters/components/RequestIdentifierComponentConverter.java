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
package com.googlecode.mobilityrpc.protocol.converters.components;

import com.googlecode.mobilityrpc.protocol.converters.ComponentConverter;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import com.googlecode.mobilityrpc.protocol.protobuf.ComponentRequestIdentifier;

/**
 * @author Niall Gallagher
 */
public class RequestIdentifierComponentConverter implements ComponentConverter<RequestIdentifier, ComponentRequestIdentifier.RequestIdentifier, ComponentRequestIdentifier.RequestIdentifier.Builder> {

    private final UuidComponentConverter uuidConverter = new UuidComponentConverter();

    public RequestIdentifier convertFromProtobuf(ComponentRequestIdentifier.RequestIdentifier protobuf) {
        String requestLabel = protobuf.getRequestLabel();
        return new RequestIdentifier(
                uuidConverter.convertFromProtobuf(protobuf.getSessionId()),
                uuidConverter.convertFromProtobuf(protobuf.getRequestId()),
                requestLabel
        );
    }

    public ComponentRequestIdentifier.RequestIdentifier.Builder convertToProtobuf(RequestIdentifier object) {
        ComponentRequestIdentifier.RequestIdentifier.Builder requestIdentifierBuilder = ComponentRequestIdentifier.RequestIdentifier.newBuilder();
        requestIdentifierBuilder.setSessionId(uuidConverter.convertToProtobuf(object.getSessionId()));
        requestIdentifierBuilder.setRequestId(uuidConverter.convertToProtobuf(object.getRequestId()));
        if (object.getRequestLabel() != null) {
            requestIdentifierBuilder.setRequestLabel(object.getRequestLabel());
        }
        return requestIdentifierBuilder;
    }
}
