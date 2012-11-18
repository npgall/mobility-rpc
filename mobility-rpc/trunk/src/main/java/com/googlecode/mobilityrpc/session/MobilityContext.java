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
package com.googlecode.mobilityrpc.session;

import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.session.impl.MobilityContextInternal;

/**
 * Provides static methods which allow mobile objects which arrive on a remote machine to get a reference
 * to the {@link MobilitySession} in which they are executing, and to determine the {@link ConnectionId}
 * from which they were received.
 *
 * @author Niall Gallagher
 */
public class MobilityContext extends MobilityContextInternal {

    /**
     * Returns the {@link MobilitySession} in which the calling thread is executing.
     * @return The {@link MobilitySession} in which the calling thread is executing
     * @throws IllegalStateException If the current thread does not have a session
     */
    public static MobilitySession getCurrentSession() {
        return MobilityContextInternal.getCurrentSession();
    }

    /**
     * Returns true if the current thread has a session, otherwise false.
     * @return True if the current thread has a session, otherwise false.
     */
    public static boolean hasCurrentSession() {
        return MobilityContextInternal.hasCurrentSession();
    }

    /**
     * Returns the {@link ConnectionId} from which the current request was received.
     * @return The {@link ConnectionId} from which the current request was received
     * @throws IllegalStateException If the current thread does not have a session
     */
    public static ConnectionId getCurrentConnectionId() {
        return MobilityContextInternal.getCurrentConnectionId();
    }

    /**
     * Private constructor, not used.
     */
    MobilityContext() {

    }
}
