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
package com.googlecode.mobilityrpc.serialization.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import com.esotericsoftware.kryo.SerializationException;
import com.esotericsoftware.kryo.serialize.ReferenceFieldSerializer;
import com.googlecode.mobilityrpc.serialization.Serializer;
import de.javakaffee.kryoserializers.*;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.GregorianCalendar;

/**
 * @author Niall Gallagher
 */
public class KryoSerializer implements Serializer {

    private final ClassLoader classLoader;
    private final Kryo kryo;

    // Initial buffer size used to serialize objects...
    private final int INITIAL_BUFFER_SIZE_IN_BYTES = 32 * 1024; // 32KB
    // Max size to which buffer can grow i.e. max size of serialized object graph...
    private final int MAX_BUFFER_SIZE_IN_BYTES = 1024 * 1024 * 100; // 100MB
    // We will store buffers in a thread-local cache for reuse.
    // However we don't want to cache buffers above a certain size,
    // in case one large request would cause us to effectively lose a large amount of memory.
    // This is the size limit of the largest buffers we will cache...
    private final int MAX_CACHEABLE_BUFFER_SIZE_IN_BYTES = 64 * 1024; // 64KB


    public KryoSerializer(ClassLoader classLoader) {
        this.classLoader = classLoader;

        // Create a new instance of Objenesis...
        // Objenesis allows us to instantiate objects from classes without calling the constructors of classes.
        // This is the only way to deserialize objects properly - if we deserialized objects by calling their
        // constructors we would have no idea what code would execute in those constructors.
        // Instead we bypass constructors and create blank objects, and then we iterate the fields of the blank objects
        // setting values into each field (even final fields) as necessary...
        final ObjenesisStd objenesisStd = new ObjenesisStd();

        // Create a new instance of Kryo...
        kryo = new Kryo() {
            // ...but override Kryo's newDefaultSerializer method to use our custom serializer by default...
            @Override
            protected com.esotericsoftware.kryo.Serializer newDefaultSerializer(Class aClass) {
                // Our custom serializer is based on Kryo's ReferenceFieldSerializer (which handles cyclic graphs)...
                return new ReferenceFieldSerializer(this, aClass) {
                    // ...except that we override its newInstance method to use Objenesis to create new instances...
                    @Override
                    public <T> T newInstance(Kryo kryo, Class<T> tClass) {
                        try {
                            // Create an object instance of the class without calling its constructor...
                            return tClass.cast(objenesisStd.newInstance(tClass));
                        }
                        catch (Exception e) {
                            throw new SerializationException("Could not instantiate class using JVM-specific strategy: " + tClass.getName(), e);
                        }
                    }

                    // Additionally, configure ReferenceFieldSerializer to not ignore synthetic fields...
                    // Synthetic fields are compiler-generated fields added to inner/anonymous objects which
                    // hold a reference to their enclosing object.
                    // This is important such that if asked to serialize e.g. an anonymous Runnable object,
                    // we also serialize its enclosing object's state...
                    {
                        setIgnoreSyntheticFields(false);
                    }
                };
            }
        };
        // Configure Kryo to allow us to skip registering classes in advance,
        // such that it relies on our default serialization strategy above...
        kryo.setRegistrationOptional(true);

        // Configure Kryo to use our session class loader to load byte code from remote machines...
        kryo.setClassLoader(classLoader);

        // Register auxiliary (non-mandatory) kryo serializer plugins, which optimise for certain common object types...
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer(kryo));
        kryo.register(Class.class, new ClassSerializer(kryo));
        kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
        kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
        kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
        kryo.register(Collections.singletonList("").getClass(), new CollectionsSingletonListSerializer(kryo));
        kryo.register(Collections.singleton("").getClass(), new CollectionsSingletonSetSerializer(kryo));
        kryo.register(Collections.singletonMap("", "").getClass(), new CollectionsSingletonMapSerializer(kryo));
        kryo.register(Currency.class, new CurrencySerializer(kryo));
        kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
        kryo.register(InvocationHandler.class, new JdkProxySerializer(kryo));
        kryo.register(StringBuffer.class, new StringBufferSerializer(kryo));
        kryo.register(StringBuilder.class, new StringBuilderSerializer(kryo));
        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
        SynchronizedCollectionsSerializer.registerSerializers(kryo);
    }

    /**
     * A thread-local cache of object buffers.
     * <p/>
     * These will get cleared automatically as idle threads are shut down.
     * As long as a thread is alive it can reuse the same ObjectBuffer object, to avoid re-allocating.
     */
    private final ThreadLocal<ObjectBuffer> threadLocalObjectBuffers = new ThreadLocal<ObjectBuffer>() {
        @Override
        protected ObjectBuffer initialValue() {
            return new ObjectBuffer(kryo, INITIAL_BUFFER_SIZE_IN_BYTES, MAX_BUFFER_SIZE_IN_BYTES);
        }
    };

    @Override
    public byte[] serialize(Object object) {
        ObjectBuffer buffer = threadLocalObjectBuffers.get();
        byte[] result = buffer.writeClassAndObject(object);
        if (result.length > MAX_CACHEABLE_BUFFER_SIZE_IN_BYTES) {
            // The buffer has been enlarged for this request beyond our maximum cacheable size.
            // Remove this buffer from the cache...
            threadLocalObjectBuffers.remove();
        }
        return result;
    }

    @Override
    public Object deserialize(byte[] serializedData) {
        ObjectBuffer buffer = threadLocalObjectBuffers.get();
        if (serializedData.length > MAX_CACHEABLE_BUFFER_SIZE_IN_BYTES) {
            // The buffer will been enlarged for this request beyond our maximum cacheable size.
            // Remove this buffer from the cache...
            threadLocalObjectBuffers.remove();
        }
        return buffer.readClassAndObject(serializedData);
    }

}
