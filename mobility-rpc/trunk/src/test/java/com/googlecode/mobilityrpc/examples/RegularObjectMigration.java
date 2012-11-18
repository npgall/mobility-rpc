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

import com.googlecode.mobilityrpc.quickstart.QuickTask;

/**
 * Demonstrates that a regular object can be migrated to a remote machine.
 * <p/>
 * The example regular object is not special in any way and does not implement any particular interfaces.
 * <p/>
 * Sends the object, and calls its <code>printDetails()</code> method on the remote machine.
 *
 * @author Niall Gallagher
 */
public class RegularObjectMigration {

    static class RegularObject {

        private String name = "Joe Bloggs";
        private String address = "Sesame Street";

        public void printDetails() {
            System.out.println(name);
            System.out.println(address);
        }
    }

    public static void main(String[] args) {
        final RegularObject regularObject = new RegularObject();
        QuickTask.execute("bob", new Runnable() { public void run() {
                regularObject.printDetails();
        }});
    }
}
