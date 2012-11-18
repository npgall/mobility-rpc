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
package com.googlecode.mobilityrpc.common;

/**
 * Implemented by objects which have an {@code init()} method and a {@code destroy()} method which
 * must be called before and after the objects can be used, to initialize and destroy resources they need.
 * <p/>
 * Such objects can be said to be "managed" by some other object e.g. the life cycle of connections might be managed
 * by a connection controller.
 * <p/>
 * This is a combination of {@link Initializable} and {@link Destroyable}.
 *
 * @author Niall Gallagher
 */
public interface Managed extends Initializable, Destroyable {

}
