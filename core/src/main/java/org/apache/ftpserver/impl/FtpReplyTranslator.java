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
import java.net.SocketAddress;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.message.MessageResource;
import org.apache.ftpserver.util.DateUtils;

/**
 * A utility class for returning translated messages. The utility method,
 * <code>translateMessage</code> also expands any variables in the message.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 *
 */

public class FtpReplyTranslator {

    public static final String CLIENT_ACCESS_TIME = "client.access.time";

    public static final String CLIENT_CON_TIME = "client.con.time";

    public static final String CLIENT_DIR = "client.dir";

    public static final String CLIENT_HOME = "client.home";

    public static final String CLIENT_IP = "client.ip";

    public static final String CLIENT_LOGIN_NAME = "client.login.name";

    public static final String CLIENT_LOGIN_TIME = "client.login.time";

    public static final String OUTPUT_CODE = "output.code";

    public static final String OUTPUT_MSG = "output.msg";

    public static final String REQUEST_ARG = "request.arg";

    public static final String REQUEST_CMD = "request.cmd";

    public static final String REQUEST_LINE = "request.line";

    // /////////////////////// All Server Vatiables /////////////////////////
    public static final String SERVER_IP = "server.ip";

    public static final String SERVER_PORT = "server.port";

    public static final String STAT_CON_CURR = "stat.con.curr";

    public static final String STAT_CON_TOTAL = "stat.con.total";

    public static final String STAT_DIR_CREATE_COUNT = "stat.dir.create.count";

    public static final String STAT_DIR_DELETE_COUNT = "stat.dir.delete.count";

    public static final String STAT_FILE_DELETE_COUNT = "stat.file.delete.count";

    public static final String STAT_FILE_DOWNLOAD_BYTES = "stat.file.download.bytes";

    public static final String STAT_FILE_DOWNLOAD_COUNT = "stat.file.download.count";

    public static final String STAT_FILE_UPLOAD_BYTES = "stat.file.upload.bytes";

    public static final String STAT_FILE_UPLOAD_COUNT = "stat.file.upload.count";

    public static final String STAT_LOGIN_ANON_CURR = "stat.login.anon.curr";

    public static final String STAT_LOGIN_ANON_TOTAL = "stat.login.anon.total";

    public static final String STAT_LOGIN_CURR = "stat.login.curr";

    public static final String STAT_LOGIN_TOTAL = "stat.login.total";

    public static final String STAT_START_TIME = "stat.start.time";

    /**
     * Returns the translated message.
     *
     * @param session
     *            the FTP session for which a reply is to be sent
     * @param request
     *            the FTP request object
     * @param context
     *            the FTP server context
     * @param code
     *            the reply code
     * @param subId
     *            the ID of the sub message
     * @param basicMsg
     *            the basic message
     * @return the translated message
     */
    public static String translateMessage(FtpIoSession session, FtpRequest request, FtpServerContext context,
                int code, String subId, String basicMsg) {
        MessageResource resource = context.getMessageResource();
        String lang = session.getLanguage();

        String msg = null;

        if (resource != null) {
            msg = resource.getMessage(code, subId, lang);
        }

        if (msg == null) {
            msg = "";
        }

        msg = replaceVariables(session, request, context, code, basicMsg, msg);

        return msg;
    }

    /**
     * Replace server variables.
     */
    private static String replaceVariables(FtpIoSession session,
        FtpRequest request, FtpServerContext context, int code,
        String basicMsg, String str) {

        int startIndex = 0;
        int openIndex = str.indexOf('{', startIndex);
        if (openIndex == -1) {
            return str;
        }

        int closeIndex = str.indexOf('}', startIndex);
        if ((closeIndex == -1) || (openIndex > closeIndex)) {
            return str;
        }

        StringBuilder sb = new StringBuilder(128);
        sb.append(str.substring(startIndex, openIndex));

        while (true) {
            String varName = str.substring(openIndex + 1, closeIndex);
            sb.append(getVariableValue(session, request, context, code,
                basicMsg, varName));

            startIndex = closeIndex + 1;
            openIndex = str.indexOf('{', startIndex);

            if (openIndex == -1) {
                sb.append(str.substring(startIndex));
                break;
            }

            closeIndex = str.indexOf('}', startIndex);

            if ((closeIndex == -1) || (openIndex > closeIndex)) {
                sb.append(str.substring(startIndex));
                break;
            }

            sb.append(str.substring(startIndex, openIndex));
        }

        return sb.toString();
    }

    /**
     * Get the variable value.
     */
    private static String getVariableValue(FtpIoSession session,
        FtpRequest request, FtpServerContext context, int code,
        String basicMsg, String varName) {
        String varVal = null;

        if (varName.startsWith("output.")) {
            // all output variables
            varVal = getOutputVariableValue(session, code, basicMsg, varName);
        } else if (varName.startsWith("server.")) {
            // all server variables
            varVal = getServerVariableValue(session, varName);
        } else if (varName.startsWith("request.")) {
            // all request variables
            varVal = getRequestVariableValue(session, request, varName);
        } else if (varName.startsWith("stat.")) {
            // all statistical variables
            varVal = getStatisticalVariableValue(session, context, varName);
        } else if (varName.startsWith("client.")) {
            // all client variables
            varVal = getClientVariableValue(session, varName);
        }

        if (varVal == null) {
            varVal = "";
        }

        return varVal;
    }

    /**
     * Get client variable value.
     */
    private static String getClientVariableValue(FtpIoSession session,
        String varName) {

        switch (varName) {
            case CLIENT_IP:
                // client ip
                if (session.getRemoteAddress() instanceof InetSocketAddress) {
                    InetSocketAddress remoteSocketAddress = (InetSocketAddress) session.getRemoteAddress();
                    return remoteSocketAddress.getAddress().getHostAddress();
                } else {
                    return null;
                }

            case CLIENT_CON_TIME:
                // client connection time
                return DateUtils.getISO8601Date(session.getCreationTime());

            case CLIENT_LOGIN_NAME:
                // client login name
                return session.getUser().getName();

            case CLIENT_LOGIN_TIME:
                // client login time
                return DateUtils.getISO8601Date(session.getLoginTime().getTime());

            case CLIENT_ACCESS_TIME:
                // client last access time
                return DateUtils.getISO8601Date(session.getLastAccessTime().getTime());

            case CLIENT_HOME:
                // client home
                return session.getUser().getHomeDirectory();

            case CLIENT_DIR:
                // client directory
                FileSystemView fsView = session.getFileSystemView();

                if (fsView != null) {
                    try {
                        return fsView.getWorkingDirectory().getAbsolutePath();
                    } catch (Exception ex) {
                        return "";
                    }
                }

            default:
                return null;
        }
    }

    /**
     * Get output variable value.
     */
    private static String getOutputVariableValue(FtpIoSession session, int code, String basicMsg, String varName) {

        switch (varName) {
            case OUTPUT_CODE:
                // output code
                return String.valueOf(code);

            case OUTPUT_MSG:
                // output message
                return basicMsg;

            default:
                return null;
        }
    }

    /**
     * Get request variable value.
     */
    private static String getRequestVariableValue(FtpIoSession session,
        FtpRequest request, String varName) {

        if (request == null) {
            return "";
        }

        switch (varName) {
            case REQUEST_LINE:
                // request line
                return request.getRequestLine();

            case REQUEST_CMD:
                // request command
                return request.getCommand();

            case REQUEST_ARG:
                // request argument
                return request.getArgument();

            default:
                return null;
        }
    }

    /**
     * Get server variable value.
     */
    private static String getServerVariableValue(FtpIoSession session, String varName) {

        SocketAddress localSocketAddress = session.getLocalAddress();

        if (localSocketAddress instanceof InetSocketAddress) {
            InetSocketAddress localInetSocketAddress = (InetSocketAddress) localSocketAddress;

            switch (varName) {
                case SERVER_IP:
                    // server address
                    InetAddress addr = localInetSocketAddress.getAddress();

                    if (addr != null) {
                        return addr.getHostAddress();
                    } else {
                        return null;
                    }

                case SERVER_PORT:
                    // server port
                    return String.valueOf(localInetSocketAddress.getPort());

                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get statistical connection variable value.
     */
    private static String getStatisticalConnectionVariableValue(
        FtpIoSession session, FtpServerContext context, String varName) {
        FtpStatistics stat = context.getFtpStatistics();

        switch (varName) {
            case STAT_CON_TOTAL:
                // total connection number
                return String.valueOf(stat.getTotalConnectionNumber());

            case STAT_CON_CURR:
                // current connection number
                return String.valueOf(stat.getCurrentConnectionNumber());

            default:
                return null;
        }
    }

    /**
     * Get statistical directory variable value.
     */
    private static String getStatisticalDirectoryVariableValue(
        FtpIoSession session, FtpServerContext context, String varName) {
        FtpStatistics stat = context.getFtpStatistics();

        switch (varName) {
            case STAT_DIR_CREATE_COUNT:
                // total directory created
                return String.valueOf(stat.getTotalDirectoryCreated());

            case STAT_DIR_DELETE_COUNT:
                // total directory removed
                return String.valueOf(stat.getTotalDirectoryRemoved());

            default:
                return null;
        }
    }

    /**
     * Get statistical file variable value.
     */
    private static String getStatisticalFileVariableValue(FtpIoSession session,
        FtpServerContext context, String varName) {
        FtpStatistics stat = context.getFtpStatistics();

        switch (varName) {
            case STAT_FILE_UPLOAD_COUNT:
                // total number of file upload
                return String.valueOf(stat.getTotalUploadNumber());

            case STAT_FILE_UPLOAD_BYTES:
                // total bytes uploaded
                return String.valueOf(stat.getTotalUploadSize());

            case STAT_FILE_DOWNLOAD_COUNT:
                // total number of file download
                return String.valueOf(stat.getTotalDownloadNumber());

            case STAT_FILE_DOWNLOAD_BYTES:
                // total bytes downloaded
                return String.valueOf(stat.getTotalDownloadSize());

            case STAT_FILE_DELETE_COUNT:
                // total number of files deleted
                return String.valueOf(stat.getTotalDeleteNumber());

            default:
                return null;
        }
    }

    /**
     * Get statistical login variable value.
     */
    private static String getStatisticalLoginVariableValue(
        FtpIoSession session, FtpServerContext context, String varName) {
        FtpStatistics stat = context.getFtpStatistics();

        switch (varName) {
            case STAT_LOGIN_TOTAL:
                // total login number
                return String.valueOf(stat.getTotalLoginNumber());

            case STAT_LOGIN_CURR:
                // current login number
                return String.valueOf(stat.getCurrentLoginNumber());

            case STAT_LOGIN_ANON_TOTAL:
                // total anonymous login number
                return String.valueOf(stat.getTotalAnonymousLoginNumber());

            case STAT_LOGIN_ANON_CURR:
                // current anonymous login number
                return String.valueOf(stat.getCurrentAnonymousLoginNumber());

            default:
                return null;
        }
    }

    /**
     * Get statistical variable value.
     */
    private static String getStatisticalVariableValue(FtpIoSession session,
        FtpServerContext context, String varName) {
        FtpStatistics stat = context.getFtpStatistics();

        String varVal = null;

        if (varName.equals(STAT_START_TIME)) {
            // server start time
            varVal = DateUtils.getISO8601Date(stat.getStartTime().getTime());
        } else if (varName.startsWith("stat.con")) {
            // connection statistical variables
            varVal = getStatisticalConnectionVariableValue(session, context, varName);
        } else if (varName.startsWith("stat.login.")) {
            // login statistical variables
            varVal = getStatisticalLoginVariableValue(session, context, varName);
        } else if (varName.startsWith("stat.file")) {
            // file statistical variable
            varVal = getStatisticalFileVariableValue(session, context, varName);
        } else if (varName.startsWith("stat.dir.")) {
            // directory statistical variable
            varVal = getStatisticalDirectoryVariableValue(session, context, varName);
        }

        return varVal;
    }
}
