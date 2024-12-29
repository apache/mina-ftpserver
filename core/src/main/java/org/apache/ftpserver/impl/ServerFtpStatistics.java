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

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpStatistics;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * This is same as <code>org.apache.ftpserver.ftplet.FtpStatistics</code> with
 * added observer and setting values functionalities.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public interface ServerFtpStatistics extends FtpStatistics {

    /**
     * Set statistics observer.
     *
     * @param observer observer to set
     */
    void setObserver(StatisticsObserver observer);

    /**
     * Set file observer.
     *
     * @param observer The observer to set
     */
    void setFileObserver(FileObserver observer);

    /**
     * Increment upload count.
     *
     * @param session The ongoing session
     * @param file The file to upload
     * @param size The file size
     */
    void setUpload(FtpIoSession session, FtpFile file, long size);

    /**
     * Increment download count.
     *
     * @param session The ongoing session
     * @param file The file to download
     * @param size The file size
     */
    void setDownload(FtpIoSession session, FtpFile file, long size);

    /**
     * Increment make directory count.
     *
     * @param session The ongoing session
     * @param dir The directorty to create
     */
    void setMkdir(FtpIoSession session, FtpFile dir);

    /**
     * Decrement remove directory count.
     *
     * @param session The ongoing session
     * @param dir The directorty to delete
     */
    void setRmdir(FtpIoSession session, FtpFile dir);

    /**
     * Increment delete count.
     *
     * @param session The ongoing session
     * @param file The file to delete
     */
    void setDelete(FtpIoSession session, FtpFile file);

    /**
     * Increment current connection count.
     *
     * @param session The ongoing session
     */
    void setOpenConnection(FtpIoSession session);

    /**
     * Decrement close connection count.
     *
     * @param session The ongoing session
     */
    void setCloseConnection(FtpIoSession session);

    /**
     * Increment current login count.
     *
     * @param session The ongoing session
     */
    void setLogin(FtpIoSession session);

    /**
     * Increment failed login count.
     *
     * @param session The ongoing session
     */
    void setLoginFail(FtpIoSession session);

    /**
     * Decrement current login count.
     *
     * @param session The ongoing session
     */
    void setLogout(FtpIoSession session);

    /**
     * Reset all cumulative total counters. Do not reset current counters, like
     * current logins, otherwise these will become negative when someone
     * disconnects.
     */
    void resetStatisticsCounters();
}
