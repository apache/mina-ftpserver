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

import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.ssl.SslConfiguration;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Data connection configuration.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class DefaultDataConnectionConfiguration implements
        DataConnectionConfiguration {

    // maximum idle time in seconds
    private final int idleTime;
    private final SslConfiguration ssl;

    private final boolean activeEnabled;
    private final String activeLocalAddress;
    private final int activeLocalPort;
    private final boolean activeIpCheck;

    private final String passiveAddress;
    private final String passiveExternalAddress;
    private final PassivePorts passivePorts;
    private final boolean passiveIpCheck;

    private final boolean implicitSsl;

    /**
     * Internal constructor, do not use directly. Use
     * {@link DataConnectionConfigurationFactory} instead.
     *
     * @param idleTime The idle time
     * @param ssl he SSL Configuration
     * @param activeEnabled Is active mode enabled?
     * @param activeIpCheck The activa IP check
     * @param activeLocalAddress The active local address
     * @param activeLocalPort The active local port
     * @param passiveAddress The passive address
     * @param passivePorts The passive ports
     * @param passiveExternalAddress The passive external address
     * @param passiveIpCheck The passive IP check
     * @param implicitSsl Implicit SSL
     *
     */
    public DefaultDataConnectionConfiguration(int idleTime,
        SslConfiguration ssl, boolean activeEnabled, boolean activeIpCheck,
        String activeLocalAddress, int activeLocalPort, String passiveAddress,
        PassivePorts passivePorts, String passiveExternalAddress,
        boolean passiveIpCheck, boolean implicitSsl) {
        this.idleTime = idleTime;
        this.ssl = ssl;
        this.activeEnabled = activeEnabled;
        this.activeIpCheck = activeIpCheck;
        this.activeLocalAddress = activeLocalAddress;
        this.activeLocalPort = activeLocalPort;
        this.passiveAddress = passiveAddress;
        this.passivePorts = passivePorts;
        this.passiveExternalAddress = passiveExternalAddress;
        this.passiveIpCheck = passiveIpCheck;
        this.implicitSsl = implicitSsl;
    }

    /**
     * Get the maximum idle time in seconds.
     *
     * {@inheritDoc}
     */
    public int getIdleTime() {
        return idleTime;
    }

    /**
     * Is PORT enabled?
     *
     * {@inheritDoc}
     */
    public boolean isActiveEnabled() {
        return activeEnabled;
    }

    /**
     * Check the PORT IP?
     *
     * {@inheritDoc}
     */
    public boolean isActiveIpCheck() {
        return activeIpCheck;
    }

    /**
     * Get the local address for active mode data transfer.
     *
     * {@inheritDoc}
     */
    public String getActiveLocalAddress() {
        return activeLocalAddress;
    }

    /**
     * Get the active local port number.
     *
     * {@inheritDoc}
     */
    public int getActiveLocalPort() {
        return activeLocalPort;
    }

    /**
     * Get passive host.
     *
     * {@inheritDoc}
     */
    public String getPassiveAddress() {
        return passiveAddress;
    }

    /**
     * Get external passive host.
     *
     * {@inheritDoc}
     */
    public String getPassiveExernalAddress()  {
        return passiveExternalAddress;
    }

    public boolean isPassiveIpCheck() {
        return passiveIpCheck;
    }

    /**
     * Get passive data port. Data port number zero (0) means that any available
     * port will be used.
     *
     * {@inheritDoc}
     */
    public synchronized int requestPassivePort() {
        return passivePorts.reserveNextPort();
    }

    /**
     * Retrive the passive ports configured for this data connection
     *
     * @return The String of passive ports
     */
    public String getPassivePorts() {
        return passivePorts.toString();
    }

    /**
     * Release data port
     *
     * {@inheritDoc}
     */
    public synchronized void releasePassivePort(final int port) {
        passivePorts.releasePort(port);
    }

    /**
     * Get SSL component.
     *
     * {@inheritDoc}
     */
    public SslConfiguration getSslConfiguration() {
        return ssl;
    }

    /**
     * @see org.apache.ftpserver.DataConnectionConfiguration#isImplicitSsl()
     *
     * {@inheritDoc}
     */
    public boolean isImplicitSsl() {
        return implicitSsl;
    }
}
