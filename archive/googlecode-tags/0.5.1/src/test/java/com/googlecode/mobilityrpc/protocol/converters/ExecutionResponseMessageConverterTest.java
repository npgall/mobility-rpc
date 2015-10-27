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

import com.googlecode.mobilityrpc.protocol.converters.messages.ExecutionResponseMessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionResponse;
import com.googlecode.mobilityrpc.protocol.pojo.RequestIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.SerializationFormat;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Niall Gallagher
 */
public class ExecutionResponseMessageConverterTest {

    @Test
    public void testMessageExecutionResultConverter() {
        ExecutionResponseMessageConverter converter = new ExecutionResponseMessageConverter();
        UUID sessionId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        ExecutionResponse input = new ExecutionResponse(
                ExecutionResponse.ExecutionOutcome.VALUE_RETURNED,
                new byte[] {1,2,3,4,5},
                SerializationFormat.KRYO,
                new RequestIdentifier(sessionId, requestId, "foo")
        );

        byte[] serialized = converter.convertToProtobuf(input);
        System.out.println("Serialized to: " + serialized.length + " bytes");
        ExecutionResponse output = converter.convertFromProtobuf(serialized);
        System.out.println("Output: " + output);

        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, output.getSerializedReturnObject());
        assertEquals(SerializationFormat.KRYO, output.getSerializationFormat());
        assertEquals(ExecutionResponse.ExecutionOutcome.VALUE_RETURNED, output.getExecutionOutcome());
        assertEquals(new RequestIdentifier(sessionId, requestId, "foo"), output.getRequestIdentifier());
    }
}
