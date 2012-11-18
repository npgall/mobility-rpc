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
package com.googlecode.mobilityrpc.quickstart;

import com.googlecode.mobilityrpc.MobilityRPC;
import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.session.MobilityContext;
import com.googlecode.mobilityrpc.session.MobilitySession;

import java.util.concurrent.Callable;

/**
 * A simplified API for sending and executing one-off tasks ({@link Runnable} or {@link Callable} objects) on
 * remote machines.
 * <p/>
 * 
 * @author Niall Gallagher
 */
public class QuickTask {

    /**
     * Transfers the given <code>Runnable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Runnable#run()} method) on the remote machine.
     * <p/>
     * Connects to the remote machine on the default port 5739.
     *
     * @param address The address (ip or name) of the remote machine
     * @param runnable The object to send and execute on the remote machine
     */
    public static void execute(String address, Runnable runnable) {
        execute(new ConnectionId(address, EmbeddedMobilityServer.DEFAULT_PORT), runnable);
    }

    /**
     * Transfers the given <code>Runnable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Runnable#run()} method) on the remote machine.
     *
     * @param connectionId The address/port of the remote machine
     * @param runnable The object to send and execute on the remote machine
     */
    public static void execute(ConnectionId connectionId, Runnable runnable) {
        MobilitySession session = MobilityRPC.newController().newSession();
        try {
            session.execute(connectionId, new SessionReleasingRunnable(runnable));
        }
        finally {
            session.getMobilityController().destroy();
        }
    }

    /**
     * Transfers the given <code>Callable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Callable#call()} method) on the remote machine.
     * <p/>
     * Transfers the object returned by the <code>call</code> method on the remote machine, and any objects it
     * references, back to the local application.
     * <p/>
     * Connects to the remote machine on the default port 5739.
     *
     * @param address The address (ip or name) of the remote machine
     * @param callable The object to send to the remote machine
     * @return The object returned by the {@link Callable#call()} method on the remote machine (transferred back to
     * this machine)
     */
    public static <T> T execute(String address, Callable<T> callable) {
        return execute(new ConnectionId(address, EmbeddedMobilityServer.DEFAULT_PORT), callable);
    }

    /**
     * Transfers the given <code>Callable</code> object, and any objects it references, to the given remote machine,
     * and executes it (calls the {@link Callable#call()} method) on the remote machine.
     * <p/>
     * Transfers the object returned by the <code>call</code> method on the remote machine, and any objects it
     * references, back to the local application.
     *
     * @param connectionId The address/port of the remote machine
     * @param callable The object to send to the remote machine
     * @return The object returned by the {@link Callable#call()} method on the remote machine (transferred back to
     * this machine)
     */
    public static <T> T execute(ConnectionId connectionId, Callable<T> callable) {
        MobilitySession session = MobilityRPC.newController().newSession();
        try {
            return session.execute(connectionId, new SessionReleasingCallable<T>(callable));
        }
        finally {
            session.getMobilityController().destroy();
        }
    }

    /**
     * A Runnable which wraps another runnable, and when invoked, runs the wrapped runnable, then releases
     * the current MobilitySession. When run on a remote machine, this will release the remote session for
     * garbage collection.
     */
    static class SessionReleasingRunnable implements Runnable {
        private final Runnable wrapped;

        SessionReleasingRunnable(Runnable wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void run() {
            try {
                wrapped.run();
            }
            finally {
                MobilityContext.getCurrentSession().release();
            }
        }
    }

    /**
     * A Callable which wraps another callable, and when invoked, runs the wrapped callable, then releases
     * the current MobilitySession. When run on a remote machine, this will release the remote session for
     * garbage collection.
     */
    static class SessionReleasingCallable<T> implements Callable<T> {
        private final Callable<T> wrapped;

        SessionReleasingCallable(Callable<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public T call() throws Exception {
            try {
                return wrapped.call();
            }
            finally {
                MobilityContext.getCurrentSession().release();
            }
        }
    }

    /**
     * Private constructor, not used.
     */
    QuickTask() {

    }
}
