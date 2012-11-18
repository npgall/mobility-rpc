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

import com.googlecode.mobilityrpc.common.Managed;
import com.googlecode.mobilityrpc.network.Connection;

/**
 * An internal interface combining the public Connection interface with several internal interfaces.
 *
 * @author Niall Gallagher
 */
public interface ConnectionInternal extends Connection, Managed {
}
