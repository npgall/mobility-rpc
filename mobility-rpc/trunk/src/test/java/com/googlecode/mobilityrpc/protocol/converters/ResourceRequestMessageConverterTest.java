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

import com.googlecode.mobilityrpc.protocol.converters.messages.ResourceRequestMessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.ResourceRequest;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Niall Gallagher
 */
public class ResourceRequestMessageConverterTest {

    @Test
    public void testMessageResourceRequestConverter() {
        ResourceRequestMessageConverter converter = new ResourceRequestMessageConverter();
        UUID sessionId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        ResourceRequest input = new ResourceRequest(Arrays.asList("foo, bar"), new RequestIdentifier(sessionId, requestId, "foo"));
        byte[] serialized = converter.convertToProtobuf(input);
        System.out.println("Serialized to: " + serialized.length + " bytes");
        ResourceRequest output = converter.convertFromProtobuf(serialized);
        System.out.println("Output: " + output);

        assertEquals(Arrays.asList("foo, bar"), output.getResourceNames());
        assertEquals(new RequestIdentifier(sessionId, requestId, "foo"), output.getRequestIdentifier());
    }
}
