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
package com.googlecode.mobilityrpc.controller.impl;

import com.googlecode.mobilityrpc.controller.MobilityController;
import com.googlecode.mobilityrpc.network.impl.IncomingMessageHandler;
import com.googlecode.mobilityrpc.network.impl.OutgoingMessageHandler;
import com.googlecode.mobilityrpc.session.impl.MobilitySessionInternal;

import java.util.UUID;

/**
 * A combination of several internal interfaces implemented by the mobility controller, most of which are not part
 * of the public API. Only {@link com.googlecode.mobilityrpc.controller.MobilityController} is part of the public API.
 *
 * @author Niall Gallagher
 */
public interface MobilityControllerInternal extends MobilityController, IncomingMessageHandler, OutgoingMessageHandler {

    public MobilitySessionInternal getMessageHandlingSession(UUID sessionId);

}
