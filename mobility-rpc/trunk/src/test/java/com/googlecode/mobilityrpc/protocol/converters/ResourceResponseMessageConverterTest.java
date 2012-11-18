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
package com.googlecode.mobilityrpc.protocol.converters;

import com.googlecode.mobilityrpc.protocol.converters.messages.ResourceResponseMessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.ResourceResponse;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Niall Gallagher
 */
public class ResourceResponseMessageConverterTest {

    @Test
    public void testResourceResponseMessageConverter() {
        ResourceResponseMessageConverter converter = new ResourceResponseMessageConverter();
        UUID sessionId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        ResourceResponse input = new ResourceResponse(
                Arrays.asList(
                    new ResourceResponse.ResourceData("foo", new byte[]{1,2}),
                    new ResourceResponse.ResourceData("bar", new byte[]{3,4,5})
                ),
                new RequestIdentifier(sessionId, requestId, "foo")
        );
        byte[] serialized = converter.convertToProtobuf(input);
        System.out.println("Serialized to: " + serialized.length + " bytes");
        ResourceResponse output = converter.convertFromProtobuf(serialized);
        System.out.println("Output: " + output);

        assertEquals(input.getResourceDataResponses(), output.getResourceDataResponses());
        assertEquals(new RequestIdentifier(sessionId, requestId, "foo"), output.getRequestIdentifier());
    }
}
