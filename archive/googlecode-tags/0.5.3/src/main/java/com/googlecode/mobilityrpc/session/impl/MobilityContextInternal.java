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
package com.googlecode.mobilityrpc.session.impl;

import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.session.MobilityContext;
import com.googlecode.mobilityrpc.session.MobilitySession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A "back-end" implementation of {@link MobilityContext} with package-private setter methods to allow
 * {@link MobilitySession} (in this same pacakge) to set thread-local values while keeping the setters methods
 * non-public.
 *
 * @author Niall Gallagher
 */
public class MobilityContextInternal {

    private static final ThreadLocal<MobilitySession> threadLocalSessions = new ThreadLocal<MobilitySession>();
    private static final ThreadLocal<ConnectionId> threadLocalConnectionIds = new ThreadLocal<ConnectionId>();

    protected static MobilitySession getCurrentSession() {
        MobilitySession currentSession = threadLocalSessions.get();
        if (currentSession == null) {
            throw new IllegalStateException("No current session");
        }
        return currentSession;
    }

    protected static boolean hasCurrentSession() {
        MobilitySession currentSession = threadLocalSessions.get();
        return currentSession != null;
    }

    static void setCurrentSession(MobilitySession session) {
        threadLocalSessions.set(session);
    }

    protected static ConnectionId getCurrentConnectionId() {
        ConnectionId currentConnectionId = threadLocalConnectionIds.get();
        if (currentConnectionId == null) {
            throw new IllegalStateException("No current connection id");
        }
        return currentConnectionId;
    }

    static void setCurrentConnectionId(ConnectionId connectionId) {
        threadLocalConnectionIds.set(connectionId);
    }
    /**
     * Private constructor, not used.
     */
    protected MobilityContextInternal() {

    }

}
