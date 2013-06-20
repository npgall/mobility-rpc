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
package com.googlecode.mobilityrpc.examples;

import com.googlecode.mobilityrpc.common.util.IOUtil;
import com.googlecode.mobilityrpc.quickstart.QuickTask;
import com.googlecode.mobilityrpc.quickstart.util.LoggingUtil;

import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Prints a text file loaded from the classpath on the local machine to the console on a remote machine.
 *
 * @author Niall Gallagher
 */
public class LoadClasspathResource {

    public static void main(String[] args) {
        LoggingUtil.setLibraryLoggingLevel(Level.FINE);
        LoggingUtil.setSingleLineLoggingFormat();
        QuickTask.execute("bob", new Runnable() {
            public void run() {
                // Request non-cached resource URL...
                // Should resemble: ﻿mobility-rpc://[192.168.56.1:52671:0]/5f088ec8-4f71-4fae-a89b-56a0b408dcbe/test-resource.txt
                System.out.println(getClass().getClassLoader().getResource("test-resource.txt"));

                // Request cached resource URL...
                // Should resemble: ﻿mobility-rpc://[local-cache:0:0]/5f088ec8-4f71-4fae-a89b-56a0b408dcbe/test-resource.txt
                System.out.println(getClass().getClassLoader().getResource("test-resource.txt"));
                
                // Request the resource content (should be cached with content locally)...
                System.out.println(loadTextFileFromClasspath(getClass().getClassLoader(), "test-resource.txt"));
            }
        });
    }

    static String loadTextFileFromClasspath(ClassLoader classLoader, String resourceName) {
        InputStream is = null;
        try {
            is = classLoader.getResourceAsStream(resourceName);
            if (is == null) {
                return null;
            }
            Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to load resource '" + resourceName + "' via class loader: " + classLoader);
        }
        finally {
            IOUtil.closeQuietly(is);
        }
    }
}
