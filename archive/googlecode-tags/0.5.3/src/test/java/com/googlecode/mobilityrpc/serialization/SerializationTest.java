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
package com.googlecode.mobilityrpc.serialization;

import com.googlecode.mobilityrpc.serialization.impl.KryoSerializer;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * Unit test for serialization/deserialization.
 */
public class SerializationTest {

    /**
     * Tests assigning values to final fields in the deserialized object.
     */
    @Test
    public void testSerializer_RestoreFinalFields() {
        Serializer serializer = new KryoSerializer(getClass().getClassLoader());
        Foo foo = new Foo(5);
        byte[] serializedData = serializer.serialize(foo);
        Object deserialized = serializer.deserialize(serializedData);
        // Foo implements equals() so this will test assignment to final field bar...
        assertEquals(foo, deserialized);
    }

    /**
     * This tests the serialization of synthetic fields, the implicit reference in inner classes to their
     * enclosing object.
     * <p/>
     * From an inner class, the enclosing object can be accessed via {@code OuterClass.this}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSerializer_InnerClassWithSyntheticReference() {
        Serializer serializer = new KryoSerializer(getClass().getClassLoader());

        class Outer {
            private final Foo foo = new Foo(5);

            private final Callable<String> callable = new Callable<String>() {
                @Override
                public String call() {
                    return "foo=" + foo + ", outer=" + Outer.this;
                }

                @Override
                public String toString() {
                    return "baz";
                }
            };

            @Override
            public String toString() {
                return "Outer{" +
                        "foo=" + foo +
                        ", callable=" + callable +
                        '}';
            }
        }
        byte[] serializedData = serializer.serialize(new Outer().callable);
        Object deserialized = serializer.deserialize(serializedData);
        Exception unexpected = null;
        String output = null;
        try {
            //noinspection unchecked
            output = ((Callable<String>)deserialized).call();
        }
        catch (Exception e) {
            unexpected = e;
        }
        assertNull("should not throw an exception", unexpected);
        assertEquals("foo=Foo{bar=5}, outer=Outer{foo=Foo{bar=5}, callable=baz}", output);
    }

    /**
     * Tests the ability to serialize null.
     */
    @Test
    public void testSerializer_SerializeNull() {
        Serializer serializer = new KryoSerializer(getClass().getClassLoader());
        byte[] serializedData = serializer.serialize(null);
        assertNotNull(serializedData);
    }
}
