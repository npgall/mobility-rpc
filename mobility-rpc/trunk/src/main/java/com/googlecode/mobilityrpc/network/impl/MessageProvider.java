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
package com.googlecode.mobilityrpc.network.impl;

/**
 * An internal interface used by the framework - implemented by objects which can provide messages, for example
 * this can wrap a queue of messages waiting to be processed.
 *
 * @author Niall Gallagher
 */
public interface MessageProvider<T> {

    /**
     * Returns the next message from the source, blocking until one becomes available if necessary.
     * @return The next message from the source.
     */
    public T getNextMessage();
}
