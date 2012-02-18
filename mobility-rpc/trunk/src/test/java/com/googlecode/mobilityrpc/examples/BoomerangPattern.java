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
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * @author Niall Gallagher
 */
public class BoomerangPattern {

    static class BoomerangObject implements Callable<BoomerangObject> {

        private Properties someData;
        private InetAddress someOtherData;

        public BoomerangObject call() throws Exception {
            someData = System.getProperties();
            someOtherData = InetAddress.getLocalHost();
            return this;
        }
    }

    public static void main(String[] args) {
        BoomerangObject boomerangObject = AdHocTask.execute(
            new ConnectionId("192.168.56.102", 5739), new BoomerangObject()
        );
        System.out.println(boomerangObject.someData);
        System.out.println(boomerangObject.someOtherData);
    }
}
