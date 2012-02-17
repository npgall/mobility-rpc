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
package com.googlecode.mobilityrpc.quickstart;

import com.googlecode.mobilityrpc.execution.ExecutionCoordinator;
import com.googlecode.mobilityrpc.execution.impl.ExecutionCoordinatorImpl;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionMode;
import com.googlecode.mobilityrpc.session.Session;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author Niall Gallagher
 */
public class AdHocTask {

    public static void execute(ConnectionIdentifier connectionIdentifier,
                               Runnable runnable) {
        ExecutionCoordinator executionCoordinator = new ExecutionCoordinatorImpl();
        try {
            Session session = executionCoordinator.getSession(UUID.randomUUID());
            session.execute(connectionIdentifier,
                    ExecutionMode.RETURN_RESPONSE,
                    runnable
            );
        }
        finally {
            executionCoordinator.destroy();
        }
    }

    public static <T> T execute(ConnectionIdentifier connectionIdentifier,
                                Callable<T> callable) {
        ExecutionCoordinator executionCoordinator = new ExecutionCoordinatorImpl();
        try {
            Session session = executionCoordinator.getSession(UUID.randomUUID());
            return session.execute(connectionIdentifier,
                    ExecutionMode.RETURN_RESPONSE,
                    callable
            );
        }
        finally {
            executionCoordinator.destroy();
        }
    }
}
