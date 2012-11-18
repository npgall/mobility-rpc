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
package com.googlecode.mobilityrpc.network;

/**
 * Identifies the endpoint (address + port) of a connection. Conceptually very similar to
 * {@link java.net.InetSocketAddress}.
 * <p/>
 * Connection ids can be supplied to the {@link ConnectionManager} to request a connection to a specific
 * port on a remote machine, or they can be supplied to the <code>execute</code> methods in
 * {@link com.googlecode.mobilityrpc.session.MobilitySession} which will create an outgoing connection automatically.
 * <p/>
 * Connection ids are also often used in the framework itself, passed between components in
 * {@code ConnectionId}-message pairs, to indicate the connection from which a message was received or to which
 * is addressed.
 * <p/>
 * <b>Auxiliary Connections</b><br/>
 * In addition to encapsulating an address + port combination, connection ids <i>optionally</i> also encapsulate
 * an "auxiliary connection id", which is a number greater than zero chosen by the application to allow it to establish
 * and distinguish between potentially multiple <i>auxiliary</i> connections to a remote machine.
 * <p/>
 * Note that auxiliary connections are not required or recommended for most applications. In fact support for auxiliary
 * connections exists to work around limitations in TCP connections on some types of networks or specialist applications
 * only. A single multiplexed connection will normally be established by default, and so establishing multiple
 * connections is unlikely to improve bandwidth utilisation, except on very high latency connections (connections with
 * high BDP, bandwidth-delay product).
 * <p/>
 * To create an auxiliary connection, the application may create a connection id referring to a remote
 * machine as normal, but additionally supply an auxiliary connection id greater than zero to the constructor. It
 * can then send objects to the remote machine using this connection id as normal; the library
 * ({@link ConnectionManager}) will create or reuse the auxiliary connection indicated automatically.
 * <p/>
 * Note that auxiliary connection ids are not transmitted to the remote machine. They are useful within the local
 * application only. The default connection to a remote machine will always have auxiliary connection id <code>0</code>
 * on both sides. On the remote machine, when incoming <i>auxiliary</i> connections are accepted, they will be assigned
 * unique <i>negative</i> auxiliary connection ids on that machine.
 *
 * @author Niall Gallagher
 */
public class ConnectionId {

    private final String address;
    private final int port;
    private final int auxiliaryConnectionId;


    /**
     * Creates a connection id which uses the primary connection to the address and port specified.
     * @param address The ip address or name of the machine to connect to
     * @param port The port on which the machine is running a connection listener
     */
    public ConnectionId(String address, int port) {
        this(address, port, 0);
    }

    /**
     * Creates a connection id which uses an auxiliary connection to the address and port specified.
     * @param address The ip address or name of the machine to connect to
     * @param port The port on which the machine is running a connection listener
     * @param auxiliaryConnectionId A number greater than zero chosen by the application to allow it to distinguish
     * between potentially multiple auxiliary connections
     */
    public ConnectionId(String address, int port, int auxiliaryConnectionId) {
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

        ConnectionId that = (ConnectionId) o;

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
        return "ConnectionId{" +
                "address='" + address + '\'' +
                ", port=" + port +
                ", auxiliaryConnectionId=" + auxiliaryConnectionId +
                '}';
    }
}
