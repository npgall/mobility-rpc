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

import com.googlecode.mobilityrpc.network.ConnectionIdentifier;
import com.googlecode.mobilityrpc.protocol.pojo.ExecutionMode;
import com.googlecode.mobilityrpc.quickstart.AdHocTask;
import com.googlecode.mobilityrpc.quickstart.MobilityContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niall Gallagher
 */
public class MobileAgentPattern {

    static class MobileAgent implements Runnable {
        private List<String> machinesToVisit = new ArrayList<String>(
                Arrays.asList("192.168.56.103", "192.168.56.102")
        );
        private int hopNumber = 0;

        @Override
        public void run() {
            System.out.println("Hello World, this is hop number: " + (++hopNumber)
                    + " in " + MobilityContext.getCurrentSession());
            if (machinesToVisit.isEmpty()) {
                System.out.println("Ran out of machines to visit");
                return;
            }
            MobilityContext.getCurrentSession().execute(
                    new ConnectionIdentifier(machinesToVisit.remove(0), 5739),
                    ExecutionMode.RETURN_RESPONSE,
                    this
            );
        }
    }

    public static void main(String[] args) {
        AdHocTask.execute(new ConnectionIdentifier("192.168.56.102", 5739),
                new MobileAgent()
        );
    }
}
