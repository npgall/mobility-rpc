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
package com.googlecode.mobilityrpc;

import com.googlecode.mobilityrpc.controller.MobilityController;
import com.googlecode.mobilityrpc.controller.impl.MobilityControllerImpl;

/**
 * A static factory which returns a new {@link MobilityController} object, which provides the main API of the
 * Mobility-RPC library.
 *
 * @author Niall Gallagher
 */
public class MobilityRPC {

    /**
     * Returns a new {@link MobilityController} instance.
     * <p/>
     * Usually the application will want to hold on to this instance so that it can interact with the library
     * via the same controller.
     *
     * @return A new {@link MobilityController} instance
     */
    public static MobilityController newController() {
        // Return the default implementation...
        return new MobilityControllerImpl();
    }

    /**
     * Private constructor, not used.
     */
    MobilityRPC() {
    }
}
