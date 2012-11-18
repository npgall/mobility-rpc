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

import com.googlecode.mobilityrpc.protocol.converters.messages.ExecutionRequestMessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionMode;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionRequest;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.SerializationFormat;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Niall Gallagher
 */
public class ExecutionRequestMessageConverterTest {

    @Test
    public void testMessageExecutionRequestConverter() {
        ExecutionRequestMessageConverter converter = new ExecutionRequestMessageConverter();
        UUID sessionId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        ExecutionRequest input = new ExecutionRequest(
                new byte[] {1,2,3,4,5},
                SerializationFormat.KRYO,
                ExecutionMode.RETURN_RESPONSE,
                new RequestIdentifier(sessionId, requestId, "foo")
        );

        byte[] serialized = converter.convertToProtobuf(input);
        System.out.println("Serialized to: " + serialized.length + " bytes");
        ExecutionRequest output = converter.convertFromProtobuf(serialized);
        System.out.println("Output: " + output);

        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, output.getSerializedExecutableObject());
        assertEquals(SerializationFormat.KRYO, output.getSerializationFormat());
        assertEquals(ExecutionMode.RETURN_RESPONSE, output.getExecutionMode());
        assertEquals(new RequestIdentifier(sessionId, requestId, "foo"), output.getRequestIdentifier());
    }
}
