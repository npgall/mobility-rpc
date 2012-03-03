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

import com.googlecode.mobilityrpc.protocol.converters.messages.PongMessageConverter;
import com.googlecode.mobilityrpc.protocol.pojo.Pong;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Niall Gallagher
 */
public class PongMessageConverterTest {

    @Test
    public void testPongMessageConverter() {
        PongMessageConverter converter = new PongMessageConverter();
        UUID uuid = UUID.randomUUID();
        Pong input = new Pong(uuid, "foo");
        byte[] serialized = converter.convertToProtobuf(input);
        System.out.println("Serialized to: " + serialized.length + " bytes");
        Pong output = converter.convertFromProtobuf(serialized);
        System.out.println("Output: " + output);

        assertEquals(input, output);
        assertEquals("foo", output.getMessage());
    }
}
