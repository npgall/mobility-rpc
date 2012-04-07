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
package com.googlecode.mobilityrpc.examples;

import com.googlecode.mobilityrpc.quickstart.QuickTask;
import com.googlecode.mobilityrpc.session.MobilityContext;
import com.googlecode.mobilityrpc.session.MobilitySession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates an object that autonomously migrates itself around the network - a "mobile agent".
 * <p/>
 * The main method transfers the object to machine "bob", where the <code>run()</code> method is called. It prints
 * "Hello World" and its hop number, 1, to the console on "bob".
 * <p/>
 * From "bob" the object transfers itself to "alice", which is the next machine on its list of machines to visit. It
 * prints "Hello World" and its incremented hop number, 2, to the console on "alice".
 * <p/>
 * From "alice" the object transfers itself back to "bob" again, where it prints "Hello World" and an incremented
 * hop number, 3, on "bob", but then it finds that it has run out of machines to visit, so it prints "Ran out of
 * machines to visit".
 * <p/>
 * <b>Output on bob</b><br/>
 * <pre>
 * ﻿Hello World, this is hop number: 1 in MobilitySession{sessionId=836d7e5f-42ca-445f-acf0-4db525dcd6ab}
 * Hello World, this is hop number: 3 in MobilitySession{sessionId=836d7e5f-42ca-445f-acf0-4db525dcd6ab}
 * Ran out of machines to visit
 * </pre>
 * <b>Output on alice</b><br/>
 * <pre>
 * ﻿Hello World, this is hop number: 2 in MobilitySession{sessionId=836d7e5f-42ca-445f-acf0-4db525dcd6ab}
 * </pre>
 *
 * @author Niall Gallagher
 */
public class MobileAgentPattern {

    static class MobileAgent implements Runnable {
        private List<String> machinesToVisit = new ArrayList<String>(Arrays.asList("alice", "bob"));
        private int hopNumber = 0;

        public void run() {
            MobilitySession session = MobilityContext.getCurrentSession();
            System.out.println("Hello World, this is hop number: " + (++hopNumber) + " in " + session);
            if (machinesToVisit.isEmpty()) {
                System.out.println("Ran out of machines to visit");
            } else {
                // Migrate to next machine and remove from the list...
                session.execute(machinesToVisit.remove(0), this);
            }
            session.release();
        }
    }
    // Agent visits bob, then alice, then bob again...
    public static void main(String[] args) {
        QuickTask.execute("bob", new MobileAgent());
    }
}
