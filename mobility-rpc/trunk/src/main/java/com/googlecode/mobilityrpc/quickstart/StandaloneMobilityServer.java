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

import com.googlecode.mobilityrpc.quickstart.util.LoggingUtil;

import java.util.logging.*;

/**
 * Starts EmbeddedMobilityServer as a standalone application, listening on port 5739 on all network interfaces by default.
 * <p/>
 * Supply system property "-Ddebug=true" on the command line to enable debug logging.
 *
 * @author Niall Gallagher
 */
public class StandaloneMobilityServer {

    /**
     * @param args Not used
     */
    public static void main(String[] args) {
        // Set up JDK logging...
        LoggingUtil.setLibraryLoggingLevel(Boolean.getBoolean("debug") ? Level.FINER : Level.INFO);
        LoggingUtil.setSingleLineLoggingFormat();

        // Start the server...
        EmbeddedMobilityServer.start();
        System.out.println("Server started, listening on port " + EmbeddedMobilityServer.DEFAULT_PORT + " on the following addresses:");
        for (String networkAddress : EmbeddedMobilityServer.getAddresses()) {
            System.out.println(networkAddress);
        }
        System.out.println();

        // Register a shutdown hook to stop the server gracefully when JVM is shutting down...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                EmbeddedMobilityServer.stop();
                System.out.println("Server stopped");
            }
        });
    }

}
