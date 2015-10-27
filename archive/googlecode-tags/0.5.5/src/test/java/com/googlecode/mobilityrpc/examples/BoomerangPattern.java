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
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * Demonstrates the Boomerang Pattern - a {@link Callable} object which returns itself.
 * <p/>
 * Sends a Callable object to a remote machine, where it gathers some data.
 * <p/>
 * The Callable object then returns itself back to the local machine, where the data is printed to the console.
 *
 * @author Niall Gallagher
 */
public class BoomerangPattern {

    // The Boomerang pattern: a Callable object which returns itself...
    static class BoomerangObject implements Callable<BoomerangObject> {

        private Properties someData;
        private InetAddress someOtherData;

        public BoomerangObject call() throws Exception {
            someData = System.getProperties();
            someOtherData = InetAddress.getLocalHost();
            return this;
        }
    }

    // Assumes remote machine is called bob...
    public static void main(String[] args) {
        BoomerangObject boomerangObject = QuickTask.execute( "bob", new BoomerangObject());
        System.out.println(boomerangObject.someData);
        System.out.println(boomerangObject.someOtherData);
    }
}
