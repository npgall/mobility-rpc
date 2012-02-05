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
package com.googlecode.mobilityrpc.benchmarks.rmi;

import com.googlecode.mobilityrpc.execution.ExecutionCoordinator;
import com.googlecode.mobilityrpc.execution.impl.ExecutionCoordinatorImpl;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionMode;
import com.googlecode.mobilityrpc.session.Session;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Niall Gallagher
 */
public class BenchmarkMultithreaded {

    private static final int NUM_THREADS = 1;
    private static final int NUM_REQUESTS_PER_THREAD = 100000;
    private static final int REQUEST_SIZE = 1; // number of objects to send in each request

    static class RmiBenchmark {
        public static void main(String[] args) {
            try {
                // Set up RMI connection...
                final RmiServer.RmiHandler rmiHandler = (RmiServer.RmiHandler) Naming.lookup("//127.0.0.1/RmiHandler");

                final AtomicLong numIterations = new AtomicLong();
                final AtomicLong numObjectsSent = new AtomicLong();
                final AtomicLong sumOfLatencyNanos = new AtomicLong();

                class BenchmarkTask implements Callable<Collection<? extends Comparable>> {
                    @Override
                    public Collection<? extends Comparable> call() {
                        Collection<? extends Comparable> input = Util.createCollection(REQUEST_SIZE);
                        Collection<? extends Comparable> output = null;
                        long startTime = System.nanoTime();
                        for (int iterationNumber = 0; iterationNumber < NUM_REQUESTS_PER_THREAD; iterationNumber++) {
                            output = processRemotelyViaRmi(input, rmiHandler);
                        }
                        long timeTakenNanos = System.nanoTime() - startTime;
                        numIterations.addAndGet(NUM_REQUESTS_PER_THREAD);
                        numObjectsSent.addAndGet(REQUEST_SIZE * NUM_REQUESTS_PER_THREAD);
                        sumOfLatencyNanos.addAndGet(timeTakenNanos);
                        return output;
                    }
                }
                Future<Collection<? extends Comparable>> result = null;

                // Warm up (run the test code but discard results)...
                ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
                for (int i = 0; i < NUM_THREADS; i++) {
                    result = executorService.submit(new BenchmarkTask());
                }
                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

                // Run test...
                executorService = Executors.newFixedThreadPool(NUM_THREADS);
                numIterations.set(0);
                numObjectsSent.set(0);
                sumOfLatencyNanos.set(0);

                for (int i = 0; i < NUM_THREADS; i++) {
                    result = executorService.submit(new BenchmarkTask());
                }

                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

                System.out.println("Finished. Final result was: " + ((result == null) ? null : result.get()));
                System.out.println("RMI Num Threads\tRMI Request Size\tRMI Requests per sec\tRMI Latency Per Request(ns)");
                System.out.println(NUM_THREADS + "\t" + (((double)numObjectsSent.get()) / numIterations.get()) + "\t" + (numIterations.get() / (sumOfLatencyNanos.get() / 1000000000.0)) + "\t" + (((double) sumOfLatencyNanos.get()) / numIterations.get()));
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static class MobilityBenchmark {
        public static void main(String[] args) {
            try {
                // Set up Mobility connection...
                final ExecutionCoordinator executionCoordinator = new ExecutionCoordinatorImpl();
                final Session session = executionCoordinator.getSession(UUID.randomUUID());
                final ConnectionIdentifier connectionIdentifier = new ConnectionIdentifier("127.0.0.1", 5739);

                final AtomicLong numIterations = new AtomicLong();
                final AtomicLong numObjectsSent = new AtomicLong();
                final AtomicLong sumOfLatencyNanos = new AtomicLong();

                class BenchmarkTask implements Callable<Collection<? extends Comparable>> {
                    @Override
                    public Collection<? extends Comparable> call() {
                        Collection<? extends Comparable> input = Util.createCollection(REQUEST_SIZE);
                        Collection<? extends Comparable> output = null;
                        long startTime = System.nanoTime();
                        for (int iterationNumber = 0; iterationNumber < NUM_REQUESTS_PER_THREAD; iterationNumber++) {
                            output = processRemotelyViaMobility(input, session, connectionIdentifier);
                        }
                        long timeTakenNanos = System.nanoTime() - startTime;
                        numIterations.addAndGet(NUM_REQUESTS_PER_THREAD);
                        numObjectsSent.addAndGet(REQUEST_SIZE * NUM_REQUESTS_PER_THREAD);
                        sumOfLatencyNanos.addAndGet(timeTakenNanos);
                        return output;
                    }
                }
                Future<Collection<? extends Comparable>> result = null;

                // Warm up (run the test code but discard results)...
                ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
                for (int i = 0; i < NUM_THREADS; i++) {
                    result = executorService.submit(new BenchmarkTask());
                }
                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

                // Run test...
                executorService = Executors.newFixedThreadPool(NUM_THREADS);
                numIterations.set(0);
                numObjectsSent.set(0);
                sumOfLatencyNanos.set(0);

                for (int i = 0; i < NUM_THREADS; i++) {
                    result = executorService.submit(new BenchmarkTask());
                }

                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                executionCoordinator.destroy();

                System.out.println("Finished. Final result was: " + ((result == null) ? null : result.get()));
                System.out.println("Mobility Num Threads\tMobility Request Size\tMobility Requests per sec\tMobility Latency Per Request(ns)");
                System.out.println(NUM_THREADS + "\t" + (((double)numObjectsSent.get()) / numIterations.get()) + "\t" + (numIterations.get() / (sumOfLatencyNanos.get() / 1000000000.0)) + "\t" + (((double) sumOfLatencyNanos.get()) / numIterations.get()));
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable> Collection<T> processRemotelyViaRmi(final Collection<T> input, RmiServer.RmiHandler rmiHandler) {
        try {
            return rmiHandler.processRequest(input);
        }
        catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable> Collection<T> processRemotelyViaMobility(final Collection<T> input, Session session, ConnectionIdentifier connectionIdentifier) {
        return session.execute(connectionIdentifier, ExecutionMode.RETURN_RESPONSE,
            new Callable<Collection<T>>() {
                public Collection<T> call() throws Exception {
                    return ServerBusinessLogic.processRequest(input);
                }
            }
        );
    }

    static class Util {
        @SuppressWarnings("unchecked")
        static Collection<? extends Comparable> createCollection(int numItems) {
            ArrayList<Person> collection = new ArrayList<Person>();
            for (int i = 0; i < numItems; i++) {
                collection.add(new Person(
                        i,
                        "Joe_" + i,
                        "Bloggs_" + i,
                        Arrays.asList("phone_" + (i + 1), "phone_" + (i + 2)),
                        i,
                        "Street_" + i,
                        "City_" + i,
                        "Country_" + i
                ));
            }
            return collection;
        }
    }
}
