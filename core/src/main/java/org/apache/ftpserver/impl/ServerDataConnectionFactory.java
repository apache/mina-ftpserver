/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ftpserver.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.ftpserver.DataConnectionException;
import org.apache.ftpserver.ftplet.DataConnectionFactory;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 *
 */
public interface ServerDataConnectionFactory extends DataConnectionFactory {

    /**
     * Port command.
     *
     * @param address The Inet address
     */
    void initActiveDataConnection(InetSocketAddress address);

    /**
     * Initiate the passive data connection.
     *
     * @return The {@link InetSocketAddress} on which the data connection if
     *         bound.
     * @throws DataConnectionException If the passivation failed
     */
    InetSocketAddress initPassiveDataConnection()
            throws DataConnectionException;

    /**
     * Set the security protocol.
     *
     * @param secure Tell if the connection should be secure or not
     */
    void setSecure(boolean secure);

    /**
     * Sets the server's control address.
     *
     * @param serverControlAddress The control address
     */
    void setServerControlAddress(InetAddress serverControlAddress);

    void setZipMode(boolean zip);

    /**
     * Check the data connection idle status.
     *
     * @param currTime  current time
     * @return Tell if the connection has timed out
     */
    boolean isTimeout(long currTime);

    /**
     * Dispose data connection - close all the sockets.
     */
    void dispose();

    /**
     * Is secure?
     *
     * @return <code>true</code> if the connection is secure
     */
    boolean isSecure();

    /**
     * Is zip mode?
     *
     * @return <code>true</code> if the connection is in zip mode
     */
    boolean isZipMode();

    /**
     * Get client address.
     *
     * @return The Inet Address
     */
    InetAddress getInetAddress();

    /**
     * Get port number.
     *
     * @return The connection port
     */
    int getPort();
}
