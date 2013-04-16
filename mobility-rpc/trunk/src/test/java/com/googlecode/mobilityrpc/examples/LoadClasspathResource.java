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
