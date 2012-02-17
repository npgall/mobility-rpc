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
package com.googlecode.mobilityrpc.execution;

import com.googlecode.mobilityrpc.execution.impl.ExecutionCoordinatorImpl;
import com.googlecode.mobilityrpc.network.ConnectionController;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionMode;
import com.googlecode.mobilityrpc.session.Session;
import com.googlecode.mobilityrpc.util.LoggingUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Niall Gallagher
 */
public class ExecutionTest {

    private transient final Logger logger = Logger.getLogger(getClass().getName());

    private transient final ExecutorService executor = Executors.newCachedThreadPool();


    @Before
    public void setUp() {
        LoggingUtil.setSingleLineLoggingFormat();
        LoggingUtil.setAppLoggingLevel(Level.FINER);
    }

    // A test which runs the listener and client in the same JVM.
    // Note this is therefore not an ideal test. You can run a listener and client in separate JVMs manually, see below.
    @Test
    public void testExecutionRequest() {
        executor.submit(new Runnable() {
            public void run() {
                testStartListener();
            }
        });
        sleep(1);
        executor.submit(new Runnable() {
            public void run() {
                testStartClientAndSubmitRunnable();
            }
        });
        sleep(10);
    }

    @Test
    @Ignore // ignored to not run this automatically.
    // Run this for manual testing in its own JUnit process (right-click in IDE),
    // and simultaneously run testStartClientAndSubmitExecutionRequest in a separate JUnit process...
    public void testStartListener() {
        final ExecutionCoordinator executionCoordinator = new ExecutionCoordinatorImpl();
        final ConnectionController connectionController = executionCoordinator.getConnectionController();

        final ConnectionIdentifier localEndpointIdentifier = new ConnectionIdentifier("127.0.0.1", 5739);

        connectionController.bindConnectionListener(localEndpointIdentifier);
        sleep(10);
        connectionController.unbindConnectionListener(localEndpointIdentifier);
    }

    @Test
    @Ignore // ignored to not run this automatically.
    // Run this for manual testing in its own JUnit process (right-click in IDE),
    // and simultaneously run testStartListener in a separate JUnit process...
    public void testStartClientAndSubmitRunnable() {
        final ExecutionCoordinator executionCoordinator = new ExecutionCoordinatorImpl();

        Session session = executionCoordinator.getSession(UUID.fromString("1dc91c11-79f3-47fe-a77f-37277c73a929"));

        logger.log(Level.INFO, "Created session: " + session);

        session.execute(new ConnectionIdentifier("127.0.0.1", 5739), ExecutionMode.RETURN_RESPONSE,
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Hello World");
                    }
                }
        );
    }

    @Test
    @Ignore // ignored to not run this automatically.
    // Run this for manual testing in its own JUnit process (right-click in IDE),
    // and simultaneously run testStartListener in a separate JUnit process...
    public void testStartClientAndSubmitCallable() {
        final ExecutionCoordinator executionCoordinator = new ExecutionCoordinatorImpl();

        Session session = executionCoordinator.getSession(UUID.randomUUID());

        logger.log(Level.INFO, "Created session: " + session);
        final int numOne = 5;
        final int numTwo = 6;

        Integer result = session.execute(new ConnectionIdentifier("127.0.0.1", 5739), ExecutionMode.RETURN_RESPONSE,
                new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        return (numOne + numTwo) * 1000;
                    }
                }
        );
        logger.log(Level.INFO, "Result returned by callable: " + result);
    }

    @Test
    @Ignore // ignored to not run this automatically.
    // Run this for manual testing in its own JUnit process (right-click in IDE),
    // and simultaneously run testStartListener in a separate JUnit process...
    public void testStartClientAndSubmitRunnable_External() {
        final ExecutionCoordinator executionCoordinator = new ExecutionCoordinatorImpl();

//        Session session = executionCoordinator.getSession(UUID.fromString("1dc91c11-79f3-47fe-a77f-37277c73a929"));
        Session session = executionCoordinator.getSession(UUID.randomUUID());

        logger.log(Level.INFO, "Created session: " + session);

        session.execute(new ConnectionIdentifier("192.168.56.101", 5739), ExecutionMode.RETURN_RESPONSE,
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Hello World !! ");
                        JFrame window = new JFrame("Hello Remote World");
                        window.setSize(600, 400);
                        JLabel label = new JLabel("<html><center><font size=+6><b>This is a test</b></font></center></html>");
                        window.getContentPane().setLayout(new BorderLayout());
                        window.getContentPane().add(label, BorderLayout.CENTER);
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        window.setLocationRelativeTo(null);
                        window.setVisible(true);
                        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ignore) {
                        }
                        window.dispose();
                    }
                }
        );
    }

    void sleep(int numSeconds) {
        try {
            TimeUnit.SECONDS.sleep(numSeconds);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Exception while sleeping for " + numSeconds + " seconds", e);
        }
    }
}