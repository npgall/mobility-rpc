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
package com.googlecode.mobilityrpc.benchmarks.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

/**
 * @author ngallagher
 * @since 2011-09-07 14:06
 */
public class RmiServer  {

    public static void main(String[] args) {
        try {
            RmiHandler rmiServer = new RmiHandlerImpl();
            UnicastRemoteObject.unexportObject(rmiServer, true);
            RmiHandler stub = (RmiHandler) UnicastRemoteObject.exportObject(rmiServer, 5738);

            // Bind the remote object's stub in the registry...
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("RmiHandler", stub);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to start RMI RmiHandler", e);
        }
    }

    public interface RmiHandler extends Remote {
        public <T extends Comparable> Collection<T> processRequest(Collection<T> input) throws RemoteException;
    }

    public static class RmiHandlerImpl extends UnicastRemoteObject implements RmiHandler {

        public RmiHandlerImpl() throws RemoteException {
            super();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends Comparable> Collection<T> processRequest(Collection<T> input) throws RemoteException {
            return ServerBusinessLogic.processRequest(input);
        }
    }
}
