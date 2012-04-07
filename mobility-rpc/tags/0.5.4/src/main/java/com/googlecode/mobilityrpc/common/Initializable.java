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
package com.googlecode.mobilityrpc.common;

/**
 * An interface implemented by objects which have an {@code init()} method which can be called to start/prepare
 * any resources the object will need to perform its functions (connections, threads etc.).
 *
 * @author Niall Gallagher
 */
public interface Initializable {

    /**
     * Initializes the resource(s) managed by the object, opening connections, starting threads etc. as necessary.
     */
    public void init();
}
