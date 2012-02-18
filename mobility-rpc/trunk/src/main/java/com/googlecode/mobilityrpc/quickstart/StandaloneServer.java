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

import com.googlecode.mobilityrpc.controller.MobilityController;
import com.googlecode.mobilityrpc.controller.impl.MobilityControllerImpl;
import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.quickstart.util.NetworkUtil;
import com.googlecode.mobilityrpc.quickstart.util.StandaloneLoggingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * @author Niall Gallagher
 */
public class StandaloneServer {

    private volatile MobilityController mobilityController = null;

    public void startServer(Iterable<ConnectionIdentifier> bindAddresses) {
        this.mobilityController = new MobilityControllerImpl();
        for (ConnectionIdentifier bindAddress : bindAddresses) {
            mobilityController.getConnectionManager().bindConnectionListener(bindAddress);
        }
    }

    public void stopServer() {
        mobilityController.destroy();
    }

    /**
     * Starts StandaloneServer as an application, listening on port 5793 on all network interfaces by default.
     * <p/>
     * Supply system property "-Dport=x" on the command line to override the default port to listen on.<br/>
     * Supply system property "-Ddebug=true" on the command line to enable debug logging.
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        // Set up JDK logging...
        StandaloneLoggingUtil.setAppLoggingLevel("com.googlecode.mobilityrpc", Boolean.getBoolean("debug") ? Level.FINER : Level.INFO);
        StandaloneLoggingUtil.setSingleLineLoggingFormat();

        final int port = 5739;

        // Determine the IP addresses on the local machine that we should bind to...
        List<String> networkInterfaceAddresses = NetworkUtil.getAllNetworkInterfaceAddresses();
        List<ConnectionIdentifier> bindAddresses = new ArrayList<ConnectionIdentifier>();
        for (String networkAddress : networkInterfaceAddresses) {
            bindAddresses.add(new ConnectionIdentifier(networkAddress, port));
        }

        // Start the server...
        final StandaloneServer standaloneServer = new StandaloneServer();
        standaloneServer.startServer(bindAddresses);
        System.out.println("Server started, listening on port " + port + " on the following addresses:");
        for (String networkAddress : networkInterfaceAddresses) {
            System.out.println(networkAddress);
        }
        System.out.println();

        // Register a shutdown hook to stop the server gracefully when JVM is shutting down...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                standaloneServer.stopServer();
                System.out.println("Server stopped");
            }
        });
    }

}
