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
import com.googlecode.mobilityrpc.network.ConnectionId;
import com.googlecode.mobilityrpc.quickstart.AdHocTask;

/**
 * @author Niall Gallagher
 */
public class HelloWorld {

    private static final String remoteAddress = "192.168.56.102";

    public static void main(String[] args) {
        AdHocTask.execute(new ConnectionId(remoteAddress, 5739), new Runnable() {
            public void run() {
                System.out.println("Hello World");
            }
        });
    }
}
