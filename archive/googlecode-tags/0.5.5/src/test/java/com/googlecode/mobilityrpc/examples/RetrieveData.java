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
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * Retrieves system properties from the remote machine and prints to the console on the local machine.
 *
 * @author Niall Gallagher
 */
public class RetrieveData {

    public static void main(String[] args) {
        Properties data = QuickTask.execute("bob", new Callable<Properties>() {
            public Properties call() throws Exception {
                return System.getProperties();
            }
        });
        System.out.println(data);
    }
}
