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
package com.googlecode.mobilityrpc.serialization.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.factories.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.googlecode.mobilityrpc.serialization.Serializer;
import de.javakaffee.kryoserializers.*;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.GregorianCalendar;

/**
 * A serializer which serializes regular Java objects to/from binary using the
 * <a href="http://code.google.com/p/kryo/">Kryo</a> serilaization framework.
 * <p/>
 * The advantages of this framework over Java's built-in serialization, are: speed, compact data sizes, and an ability
 * to serialize regular Java objects without requiring them to implement {@link java.io.Serializable}.
 *
 * @author Niall Gallagher
 */
public class KryoSerializer implements Serializer {

    private final Kryo kryo;

    public KryoSerializer(ClassLoader classLoader) {
        kryo = new Kryo();
        kryo.setDefaultSerializer(new SerializerFactory() {
            @Override
            public com.esotericsoftware.kryo.Serializer makeSerializer(Kryo kryo, Class<?> type) {
                FieldSerializer fieldSerializer = new FieldSerializer(kryo, type);
                // Serialize (do not ignore) synthetic fields.
                // Synthetic fields are compiler-generated fields added to inner/anonymous objects which hold a
                // reference to their enclosing object.
                // This is important so that if asked to serialize e.g. an anonymous Runnable object,
                // we also serialize its enclosing object's state...
                fieldSerializer.setIgnoreSyntheticFields(false);
                return fieldSerializer;
            }
        });
        // Instantiate objects by calling a no-arg constructor by default,
        // but if the object does not have a no-arg constructor
        // fall back to instantiating via the objenesis library without calling a constructor...
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        // Configure Kryo to use our session class loader to load byte code from remote machines...
        kryo.setClassLoader(classLoader);

        // Register additional serializers which are not built-in to Kryo 3.0...
        kryo.register(Arrays.asList().getClass(), new ArraysAsListSerializer());
        kryo.register( GregorianCalendar.class, new GregorianCalendarSerializer() );
        kryo.register( InvocationHandler.class, new JdkProxySerializer() );
        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
        SynchronizedCollectionsSerializer.registerSerializers(kryo);
    }

    @Override
    public byte[] serialize(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, object);
        output.flush();
        output.close();
        return baos.toByteArray();
    }

    @Override
    public Object deserialize(byte[] serializedData) {
        Input input = new Input(new ByteArrayInputStream(serializedData));
        Object object = kryo.readClassAndObject(input);
        input.close();
        return object;
    }
}
