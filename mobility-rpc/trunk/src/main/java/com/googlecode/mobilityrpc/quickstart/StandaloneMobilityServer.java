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

import com.googlecode.mobilityrpc.quickstart.util.LoggingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.*;

/**
 * Starts EmbeddedMobilityServer as a standalone application, listening on port 5739 on all network interfaces by default.
 * <p/>
 * Adds a system tray icon which displays information about the addresses on which the library is listening, and which
 * allows the library to be shut down, if the host machine is not headless and supports system tray icons.
 * <p/>
 * Supply system property "-Ddebug=true" on the command line to enable debug logging.<br/>
 * Supply system property "-Dcom.googlecode.mobilityrpc.headless=true" to explicitly disable system tray support.
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
        System.out.println("Mobility-RPC Server started, listening on port " + EmbeddedMobilityServer.DEFAULT_PORT + " on the following addresses:");
        for (String networkAddress : EmbeddedMobilityServer.getAddresses()) {
            System.out.println(networkAddress);
        }
        System.out.println();

        // Register a shutdown hook to stop the server gracefully when JVM is shutting down...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                EmbeddedMobilityServer.stop();
                System.out.println("\nMobility-RPC Server stopped");
            }
        });
        addSystemTrayIconIfSupported();
    }

    static void addSystemTrayIconIfSupported() {
        // For Mac OS only, set application name correctly in the menu bar. Need to do this before even querying AWT...
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Mobility-RPC");
        // Check if we can add a system tray icon, and if supported add one...
        if (!GraphicsEnvironment.isHeadless() && SystemTray.isSupported() && !Boolean.getBoolean("com.googlecode.mobilityrpc.headless")) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Set system look and feel...
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                        // Prepare message which will be displayed when tray icon is clicked...
                        final StringBuilder infoMessage = new StringBuilder();
                        infoMessage.append("Mobility-RPC Standalone Server is running.\n\n");
                        infoMessage.append("Listening on port ").append(EmbeddedMobilityServer.DEFAULT_PORT).append(" on the following addresses:\n");
                        for (String networkAddress : EmbeddedMobilityServer.getAddresses()) {
                            infoMessage.append(networkAddress).append("\n");
                        }
                        infoMessage.append("\nTo shut down Mobility-RPC, select the Exit option from this menu.");

                        // Prepare graphic for the icon which will be added to system tray...
                        BufferedImage graphic;
                        {
                            final String uiResourceName = "/tray-icon.png";
                            URL iconUrl = getClass().getResource(uiResourceName);
                            if (iconUrl == null) {
                                throw new IllegalStateException("Unable to locate graphic for system tray icon using resource name: " + uiResourceName);
                            }
                            Icon icon =  new ImageIcon(iconUrl, "Mobility-RPC");
                            
                            int w = icon.getIconWidth();
                            int h = icon.getIconHeight();
                            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                            GraphicsDevice gd = ge.getDefaultScreenDevice();
                            GraphicsConfiguration gc = gd.getDefaultConfiguration();
                            BufferedImage image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
                            Graphics2D g = image.createGraphics();
                            icon.paintIcon(null, g, 0, 0);
                            g.dispose();
                            graphic = image;
                        }

                        // Prepare popup menu for tray icon when clicked, with a menu item to Exit Mobility-RPC...
                        PopupMenu popup = new PopupMenu();
                        MenuItem exitItem = new MenuItem("Exit Mobility-RPC");
                        popup.add(exitItem);

                        // Prepare system tray icon and add the popup menu to it.
                        // Also display the info message as a tooltip if the mouse hovers on the tray icon...
                        final TrayIcon trayIcon = new TrayIcon(graphic, "Mobility-RPC Standalone Server is running", popup);
                        trayIcon.setImageAutoSize(true);

                        // When the tray icon is clicked, display the information message...
                        trayIcon.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                trayIcon.displayMessage("Mobility-RPC Running", infoMessage.toString(), TrayIcon.MessageType.INFO);
                            }
                        });
                        // When "Exit Mobility-RPC" is selected, exit the JVM (this will invoke the shutdown hook)...
                        exitItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                System.exit(0);
                            }
                        });
                        // Add the icon to the system tray...
                        SystemTray.getSystemTray().add(trayIcon);

                        // Explicitly display the info message right after the tray icon is added...
                        trayIcon.displayMessage("Mobility-RPC Running", infoMessage.toString(), TrayIcon.MessageType.INFO);
                    }
                    catch (Exception e) {
                        System.err.println("Exception adding system tray icon");
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
