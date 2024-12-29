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

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * FTP statistics observer interface.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface StatisticsObserver {

    /**
     * User file upload notification.
     */
    void notifyUpload();

    /**
     * User file download notification.
     */
    void notifyDownload();

    /**
     * User file delete notification.
     */
    void notifyDelete();

    /**
     * User make directory notification.
     */
    void notifyMkdir();

    /**
     * User remove directory notification.
     */
    void notifyRmdir();

    /**
     * New user login notification.
     *
     * @param anonymous If the notification has to be anonymous
     */
    void notifyLogin(boolean anonymous);

    /**
     * Failed user login notification.
     *
     * @param address
     *            Remote address that the failure came from
     */
    void notifyLoginFail(InetAddress address);

    /**
     * User logout notification.
     *
     * @param anonymous If the notification has to be anonymous
     */
    void notifyLogout(boolean anonymous);

    /**
     * Connection open notification
     */
    void notifyOpenConnection();

    /**
     * Connection close notification
     */
    void notifyCloseConnection();

}
