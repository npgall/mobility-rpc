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
package com.googlecode.mobilityrpc.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class for mapping UUIDs to objects, such that an object with a given UUID can be retrieved later. This can
 * be useful to allow mobile objects to access other objects shared by the host application, or by other mobile objects.
 *
 * @author Niall Gallagher
 */
public class ObjectSpace {

    private static final Map<UUID, Object> uuidObjectRegistry = new ConcurrentHashMap<UUID, Object>();

    public static <T> void setUuidObject(UUID uuid, T object) {
        uuidObjectRegistry.put(uuid, object);
    }

    public static <T> T getUuidObject(Class<T> expectedType, UUID uuid) {
        return expectedType.cast(uuidObjectRegistry.get(uuid));
    }
}
