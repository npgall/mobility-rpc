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

import com.googlecode.mobilityrpc.protocol.converters.messages.ByteCodeRequestMessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.ByteCodeRequest;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Niall Gallagher
 */
public class ByteCodeRequestMessageConverterTest {

    @Test
    public void testMessageByteCodeRequestConverter() {
        ByteCodeRequestMessageConverter converter = new ByteCodeRequestMessageConverter();
        UUID sessionId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        ByteCodeRequest input = new ByteCodeRequest(Arrays.asList("foo, bar"), new RequestIdentifier(sessionId, requestId, "foo"));
        byte[] serialized = converter.convertToProtobuf(input);
        System.out.println("Serialized to: " + serialized.length + " bytes");
        ByteCodeRequest output = converter.convertFromProtobuf(serialized);
        System.out.println("Output: " + output);

        assertEquals(Arrays.asList("foo, bar"), output.getClassNames());
        assertEquals(new RequestIdentifier(sessionId, requestId, "foo"), output.getRequestIdentifier());
    }
}
