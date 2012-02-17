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
package com.googlecode.mobilityrpc.network;

/**
 * Identifies the endpoint (address + port) of a connection.
 * <p/>
 * Connection identifiers can be supplied to the {@link ConnectionController} to request a connection to a specific
 * port on a remote (or local) machine.
 * <p/>
 * Connection identifiers are also often used in the framework itself, passed between subsystems in
 * {@code ConnectionIdentifier} and message pairs, to indicate the connection from which a message was received or
 * is addressed.
 *
 * @author Niall Gallagher
 */
public class ConnectionIdentifier {

    private final String address;
    private final int port;
    private final int auxiliaryConnectionId;


    /**
     * Creates a connection identifier which uses the primary connection to the address and port specified.
     * @param address The ip address or name of the machine to connect to
     * @param port The port on which the machine is running a connection listener
     */
    public ConnectionIdentifier(String address, int port) {
        this(address, port, 0);
    }

    /**
     * Creates a connection identifier which uses an auxiliary connection to the address and port specified.
     * @param address The ip address or name of the machine to connect to
     * @param port The port on which the machine is running a connection listener
     * @param auxiliaryConnectionId A number greater than zero chosen by the application to allow it to distinguish
     * between potentially multiple auxiliary connections
     */
    public ConnectionIdentifier(String address, int port, int auxiliaryConnectionId) {
        this.address = address;
        this.port = port;
        this.auxiliaryConnectionId = auxiliaryConnectionId;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getAuxiliaryConnectionId() {
        return auxiliaryConnectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionIdentifier that = (ConnectionIdentifier) o;

        if (auxiliaryConnectionId != that.auxiliaryConnectionId) return false;
        if (port != that.port) return false;
        return address.equals(that.address);

    }

    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + port;
        result = 31 * result + auxiliaryConnectionId;
        return result;
    }

    @Override
    public String toString() {
        return "ConnectionIdentifier{" +
                "address='" + address + '\'' +
                ", port=" + port +
                ", auxiliaryConnectionId=" + auxiliaryConnectionId +
                '}';
    }
}
