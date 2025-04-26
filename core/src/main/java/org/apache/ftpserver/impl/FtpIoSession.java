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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.ftpserver.ftplet.DataType;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Structure;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.Listener;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.filter.ssl.SslFilter;
import org.slf4j.LoggerFactory;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 *
 */
public class FtpIoSession implements IoSession {
    /// Contains user name between USER and PASS commands
    /** Prefix for all the attributes*/
    public static final String ATTRIBUTE_PREFIX = "org.apache.ftpserver.";

    /** User argument attribute */
    private static final String ATTRIBUTE_USER_ARGUMENT =           ATTRIBUTE_PREFIX + "user-argument";

    /** session ID attribute */
    private static final String ATTRIBUTE_SESSION_ID =              ATTRIBUTE_PREFIX + "session-id";

    /** User attribute */
    private static final String ATTRIBUTE_USER =                    ATTRIBUTE_PREFIX + "user";

    /** Language attribute */
    private static final String ATTRIBUTE_LANGUAGE =                ATTRIBUTE_PREFIX + "language";

    /** Login time attribute */
    private static final String ATTRIBUTE_LOGIN_TIME =              ATTRIBUTE_PREFIX + "login-time";

    /** Data connection attribute */
    private static final String ATTRIBUTE_DATA_CONNECTION =         ATTRIBUTE_PREFIX + "data-connection";

    /** File system attribute */
    private static final String ATTRIBUTE_FILE_SYSTEM =             ATTRIBUTE_PREFIX + "file-system";

    /** Rename from attribute */
    private static final String ATTRIBUTE_RENAME_FROM =             ATTRIBUTE_PREFIX + "rename-from";

    /** File offset attribute */
    private static final String ATTRIBUTE_FILE_OFFSET =             ATTRIBUTE_PREFIX + "file-offset";

    /** Data type attribute */
    private static final String ATTRIBUTE_DATA_TYPE =               ATTRIBUTE_PREFIX + "data-type";

    /** Structure attribute */
    private static final String ATTRIBUTE_STRUCTURE =               ATTRIBUTE_PREFIX + "structure";

    /** Failed login attribute */
    private static final String ATTRIBUTE_FAILED_LOGINS =           ATTRIBUTE_PREFIX + "failed-logins";

    /** Listener attribute */
    private static final String ATTRIBUTE_LISTENER =                ATTRIBUTE_PREFIX + "listener";

    /** Max idle time attribute */
    private static final String ATTRIBUTE_MAX_IDLE_TIME =           ATTRIBUTE_PREFIX + "max-idle-time";

    /** Last access time attribute */
    private static final String ATTRIBUTE_LAST_ACCESS_TIME =        ATTRIBUTE_PREFIX + "last-access-time";

    /** Cached remote address attribute */
    private static final String ATTRIBUTE_CACHED_REMOTE_ADDRESS =   ATTRIBUTE_PREFIX + "cached-remote-address";

    /** The encapsulated IoSession instance */
    private final IoSession wrappedSession;

    /** The server context instance */
    private final FtpServerContext context;

    /** Last reply that was sent to the client, if any. */
    private FtpReply lastReply = null;

    /**
     * Public constructor
     *
     * @param wrappedSession The wrapped IoSession
     * @param context The server cobtext
     */
    public FtpIoSession(IoSession wrappedSession, FtpServerContext context) {
        this.wrappedSession = wrappedSession;
        this.context = context;
    }

    /* Begin wrapped IoSession methods */
    /**
     * {@inheritDoc}
     */
    public CloseFuture close() {
        return wrappedSession.close();
    }

    /**
     * {@inheritDoc}
     */
    public CloseFuture close(boolean immediately) {
        return wrappedSession.close(immediately);
    }

    /**
     * {@inheritDoc}
     */
    public CloseFuture closeNow() {
        return wrappedSession.closeNow();
    }

    /**
     * {@inheritDoc}
     */
    public CloseFuture closeOnFlush() {
        return wrappedSession.closeOnFlush();
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsAttribute(Object key) {
        return wrappedSession.containsAttribute(key);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public Object getAttachment() {
        return wrappedSession.getAttachment();
    }

    /**
     * {@inheritDoc}
     */
    public Object getAttribute(Object key) {
        return wrappedSession.getAttribute(key);
    }

    /**
     * {@inheritDoc}
     */
    public Object getAttribute(Object key, Object defaultValue) {
        return wrappedSession.getAttribute(key, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Object> getAttributeKeys() {
        return wrappedSession.getAttributeKeys();
    }

    /**
     * {@inheritDoc}
     */
    public int getBothIdleCount() {
        return wrappedSession.getBothIdleCount();
    }

    /**
     * {@inheritDoc}
     */
    public CloseFuture getCloseFuture() {
        return wrappedSession.getCloseFuture();
    }

    /**
     * {@inheritDoc}
     */
    public IoSessionConfig getConfig() {
        return wrappedSession.getConfig();
    }

    /**
     * {@inheritDoc}
     */
    public long getCreationTime() {
        return wrappedSession.getCreationTime();
    }

    /**
     * {@inheritDoc}
     */
    public IoFilterChain getFilterChain() {
        return wrappedSession.getFilterChain();
    }

    /**
     * {@inheritDoc}
     */
    public IoHandler getHandler() {
        return wrappedSession.getHandler();
    }

    /**
     * {@inheritDoc}
     */
    public long getId() {
        return wrappedSession.getId();
    }

    /**
     * {@inheritDoc}
     */
    public int getIdleCount(IdleStatus status) {
        return wrappedSession.getIdleCount(status);
    }

    /**
     * {@inheritDoc}
     */
    public long getLastBothIdleTime() {
        return wrappedSession.getLastBothIdleTime();
    }

    /**
     * {@inheritDoc}
     */
    public long getLastIdleTime(IdleStatus status) {
        return wrappedSession.getLastIdleTime(status);
    }

    /**
     * {@inheritDoc}
     */
    public long getLastIoTime() {
        return wrappedSession.getLastIoTime();
    }

    /**
     * {@inheritDoc}
     */
    public long getLastReadTime() {
        return wrappedSession.getLastReadTime();
    }

    /**
     * {@inheritDoc}
     */
    public long getLastReaderIdleTime() {
        return wrappedSession.getLastReaderIdleTime();
    }

    /**
     * {@inheritDoc}
     */
    public long getLastWriteTime() {
        return wrappedSession.getLastWriteTime();
    }

    /**
     * {@inheritDoc}
     */
    public long getLastWriterIdleTime() {
        return wrappedSession.getLastWriterIdleTime();
    }

    /**
     * {@inheritDoc}
     */
    public SocketAddress getLocalAddress() {
        return wrappedSession.getLocalAddress();
    }

    /**
     * {@inheritDoc}
     */
    public long getReadBytes() {
        return wrappedSession.getReadBytes();
    }

    /**
     * {@inheritDoc}
     */
    public double getReadBytesThroughput() {
        return wrappedSession.getReadBytesThroughput();
    }

    /**
     * {@inheritDoc}
     */
    public long getReadMessages() {
        return wrappedSession.getReadMessages();
    }

    /**
     * {@inheritDoc}
     */
    public double getReadMessagesThroughput() {
        return wrappedSession.getReadMessagesThroughput();
    }

    /**
     * {@inheritDoc}
     */
    public int getReaderIdleCount() {
        return wrappedSession.getReaderIdleCount();
    }

    /**
     * {@inheritDoc}
     */
    public SocketAddress getRemoteAddress() {
        // when closing a socket, the remote address might be reset to null
        // therefore, we attempt to keep a cached copy around

        SocketAddress address = wrappedSession.getRemoteAddress();
        if (address == null
                && containsAttribute(ATTRIBUTE_CACHED_REMOTE_ADDRESS)) {
            return (SocketAddress) getAttribute(ATTRIBUTE_CACHED_REMOTE_ADDRESS);
        } else {
            setAttribute(ATTRIBUTE_CACHED_REMOTE_ADDRESS, address);
            return address;
        }
    }

    /**
     * {@inheritDoc}
     */
    public long getScheduledWriteBytes() {
        return wrappedSession.getScheduledWriteBytes();
    }

    /**
     * {@inheritDoc}
     */
    public int getScheduledWriteMessages() {
        return wrappedSession.getScheduledWriteMessages();
    }

    /**
     * {@inheritDoc}
     */
    public IoService getService() {
        return wrappedSession.getService();
    }

    /**
     * {@inheritDoc}
     */
    public SocketAddress getServiceAddress() {
        return wrappedSession.getServiceAddress();
    }

    /**
     * {@inheritDoc}
     */
    public TransportMetadata getTransportMetadata() {
        return wrappedSession.getTransportMetadata();
    }

    /**
     * {@inheritDoc}
     */
    public int getWriterIdleCount() {
        return wrappedSession.getWriterIdleCount();
    }

    /**
     * {@inheritDoc}
     */
    public long getWrittenBytes() {
        return wrappedSession.getWrittenBytes();
    }

    /**
     * {@inheritDoc}
     */
    public double getWrittenBytesThroughput() {
        return wrappedSession.getWrittenBytesThroughput();
    }

    /**
     * {@inheritDoc}
     */
    public long getWrittenMessages() {
        return wrappedSession.getWrittenMessages();
    }

    /**
     * {@inheritDoc}
     */
    public double getWrittenMessagesThroughput() {
        return wrappedSession.getWrittenMessagesThroughput();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isClosing() {
        return wrappedSession.isClosing();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConnected() {
        return wrappedSession.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActive() {
        return wrappedSession.isActive();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isIdle(IdleStatus status) {
        return wrappedSession.isIdle(status);
    }

    /**
     * {@inheritDoc}
     */
    public ReadFuture read() {
        return wrappedSession.read();
    }

    /**
     * {@inheritDoc}
     */
    public Object removeAttribute(Object key) {
        return wrappedSession.removeAttribute(key);
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeAttribute(Object key, Object value) {
        return wrappedSession.removeAttribute(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean replaceAttribute(Object key, Object oldValue, Object newValue) {
        return wrappedSession.replaceAttribute(key, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public void resumeRead() {
        wrappedSession.resumeRead();
    }

    /**
     * {@inheritDoc}
     */
    public void resumeWrite() {
        wrappedSession.resumeWrite();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public Object setAttachment(Object attachment) {
        return wrappedSession.setAttachment(attachment);
    }

    /**
     * {@inheritDoc}
     */
    public Object setAttribute(Object key) {
        return wrappedSession.setAttribute(key);
    }

    /**
     * {@inheritDoc}
     */
    public Object setAttribute(Object key, Object value) {
        return wrappedSession.setAttribute(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public Object setAttributeIfAbsent(Object key) {
        return wrappedSession.setAttributeIfAbsent(key);
    }

    /**
     * {@inheritDoc}
     */
    public Object setAttributeIfAbsent(Object key, Object value) {
        return wrappedSession.setAttributeIfAbsent(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public void suspendRead() {
        wrappedSession.suspendRead();
    }

    /**
     * {@inheritDoc}
     */
    public void suspendWrite() {
        wrappedSession.suspendWrite();
    }

    /**
     * {@inheritDoc}
     */
    public WriteFuture write(Object message) {
        WriteFuture future = wrappedSession.write(message);
        this.lastReply = (FtpReply) message;
        return future;
    }

    /**
     * {@inheritDoc}
     */
    public WriteFuture write(Object message, SocketAddress destination) {
        WriteFuture future = wrappedSession.write(message, destination);
        this.lastReply = (FtpReply) message;
        return future;
    }

    /* End wrapped IoSession methods */
    /**
     * Reset the session state. It will remove the 'rename-from' and 'file-offset'
     * attributes from the session
     */
    public void resetState() {
        removeAttribute(ATTRIBUTE_RENAME_FROM);
        removeAttribute(ATTRIBUTE_FILE_OFFSET);
    }

    /**
     * Get the DataConnection
     *
     * @return The DataConnection instance
     */
    public synchronized ServerDataConnectionFactory getDataConnection() {
        if (containsAttribute(ATTRIBUTE_DATA_CONNECTION)) {
            return (ServerDataConnectionFactory) getAttribute(ATTRIBUTE_DATA_CONNECTION);
        } else {
            IODataConnectionFactory dataCon = new IODataConnectionFactory(context, this);
            dataCon.setServerControlAddress(((InetSocketAddress) getLocalAddress()).getAddress());
            setAttribute(ATTRIBUTE_DATA_CONNECTION, dataCon);

            return dataCon;
        }
    }

    /**
     * Get the 'file-system' attribute
     *
     * @return The 'file-system' attribute value
     */
    public FileSystemView getFileSystemView() {
        return (FileSystemView) getAttribute(ATTRIBUTE_FILE_SYSTEM);
    }

    /**
     * Get the 'user' attribute
     *
     * @return The 'user' attribute value
     */
    public User getUser() {
        return (User) getAttribute(ATTRIBUTE_USER);
    }

    /**
     * Is logged-in
     *
     * @return <code>true</code> if the user is logged in
     */
    public boolean isLoggedIn() {
        return containsAttribute(ATTRIBUTE_USER);
    }

    /**
     * Get the session listener
     *
     * @return The session listener
     */
    public Listener getListener() {
        return (Listener) getAttribute(ATTRIBUTE_LISTENER);
    }

    /**
     * Set the listener attribute
     *
     * @param listener The listener to set
     */
    public void setListener(Listener listener) {
        setAttribute(ATTRIBUTE_LISTENER, listener);
    }

    /**
     * Get a Ftp session
     *
     * @return a new Ftp session instance
     */
    public FtpSession getFtpletSession() {
        return new DefaultFtpSession(this);
    }

    /**
     * Get the session's language
     *-
     * @return The session language
     */
    public String getLanguage() {
        return (String) getAttribute(ATTRIBUTE_LANGUAGE);
    }

    /**
     * Set the session language
     *
     * @param language The language to set
     */
    public void setLanguage(String language) {
        setAttribute(ATTRIBUTE_LANGUAGE, language);

    }

    /**
     * Set the 'user' attribute
     *
     * @param user The user for this session
     */
    public void setUser(User user) {
        setAttribute(ATTRIBUTE_USER, user);

    }

    /**
     * Get the user argument
     *
     * @return The user argument to set
     */
    public String getUserArgument() {
        return (String) getAttribute(ATTRIBUTE_USER_ARGUMENT);
    }

    /**
     * Set the user argument
     *
     * @param userArgument The user argument to set
     */
    public void setUserArgument(String userArgument) {
        setAttribute(ATTRIBUTE_USER_ARGUMENT, userArgument);

    }

    /**
     * Get the max idle time
     *
     * @return The configured max idle time
     */
    public int getMaxIdleTime() {
        return (Integer) getAttribute(ATTRIBUTE_MAX_IDLE_TIME, 0);
    }

    /**
     * Set the max idle time for a session
     *
     * @param maxIdleTime Maximum time a session can idle
     */
    public void setMaxIdleTime(int maxIdleTime) {
        setAttribute(ATTRIBUTE_MAX_IDLE_TIME, maxIdleTime);

        int listenerTimeout = getListener().getIdleTimeout();

        // the listener timeout should be the upper limit, unless set to unlimited
        // if the user limit is set to be unlimited, use the listener value is the threshold
        //     (already used as the default for all sessions)
        // else, if the user limit is less than the listener idle time, use the user limit
        if (listenerTimeout <= 0
                || (maxIdleTime > 0 && maxIdleTime < listenerTimeout)) {
            wrappedSession.getConfig().setBothIdleTime(maxIdleTime);
        }
    }

    /**
     * Increment the number of failed logins
     */
    public synchronized void increaseFailedLogins() {
        int failedLogins = (Integer) getAttribute(ATTRIBUTE_FAILED_LOGINS, 0);
        failedLogins++;
        setAttribute(ATTRIBUTE_FAILED_LOGINS, failedLogins);
    }

    /**
     * Get the 'failed-logins' attribute. It contains the number
     * of failed logins during this session
     *
     * @return The number of failed logins
     */
    public int getFailedLogins() {
        return (Integer) getAttribute(ATTRIBUTE_FAILED_LOGINS, 0);
    }

    /**
     * Set the login attributes: 'login-time' and 'file-system'
     *
     * @param fsview The file system view
     */
    public void setLogin(FileSystemView fsview) {
        setAttribute(ATTRIBUTE_LOGIN_TIME, new Date());
        setAttribute(ATTRIBUTE_FILE_SYSTEM, fsview);
    }

    /**
     * Reinitialize the session. It will disconnect the user,
     * and clear the 'user', 'user-argument', 'login-time', 'file-system',
     * 'rename-from' and 'file-offset' session attributes
     */
    public void reinitialize() {
        logoutUser();
        removeAttribute(ATTRIBUTE_USER);
        removeAttribute(ATTRIBUTE_USER_ARGUMENT);
        removeAttribute(ATTRIBUTE_LOGIN_TIME);
        removeAttribute(ATTRIBUTE_FILE_SYSTEM);
        removeAttribute(ATTRIBUTE_RENAME_FROM);
        removeAttribute(ATTRIBUTE_FILE_OFFSET);
    }

    /**
     * Logout the connected user
     */
    public void logoutUser() {
        ServerFtpStatistics stats = ((ServerFtpStatistics) context.getFtpStatistics());
        if (stats != null) {
            stats.setLogout(this);
            LoggerFactory.getLogger(this.getClass()).debug("Statistics login decreased due to user logout");
        } else {
            LoggerFactory.getLogger(
                this.getClass()).warn("Statistics not available in session, can not decrease login  count");
        }
    }

    /**
     * Get the 'file-offset' attribute value. Default to 0 if none is set
     *
     * @return The 'file-offset' attribute value
     */
    public long getFileOffset() {
        return (Long) getAttribute(ATTRIBUTE_FILE_OFFSET, 0L);
    }

    /**
     * Set the 'file-offset' attribute value.
     *
     * @param fileOffset The 'file-offset' attribute value
     */
    public void setFileOffset(long fileOffset) {
        setAttribute(ATTRIBUTE_FILE_OFFSET, fileOffset);

    }

    /**
     * Get the 'rename-from' attribute
     *
     * @return The 'rename-from' attribute value
     */
    public FtpFile getRenameFrom() {
        return (FtpFile) getAttribute(ATTRIBUTE_RENAME_FROM);
    }

    /**
     * Set the 'rename-from' attribute
     *
     * @param renFr The 'rename-from' attribute value
     */
    public void setRenameFrom(FtpFile renFr) {
        setAttribute(ATTRIBUTE_RENAME_FROM, renFr);

    }

    /**
     * Get the structure attribute. We support only <code>FILE</code>
     *
     * @return The structure attribute
     */
    public Structure getStructure() {
        return (Structure) getAttribute(ATTRIBUTE_STRUCTURE, Structure.FILE);
    }

    /**
     * Set the transfert structure
     *
     * @param structure The structure (only FILE is currently supported)
     */
    public void setStructure(Structure structure) {
        setAttribute(ATTRIBUTE_STRUCTURE, structure);
    }

    /**
     * Get the data type (ascii or binary)
     *
     * @return The data type
     */
    public DataType getDataType() {
        return (DataType) getAttribute(ATTRIBUTE_DATA_TYPE, DataType.ASCII);
    }

    /**
     * Set the data type
     *
     * @param dataType The data type to use (ASCII or BINARY)
     */
    public void setDataType(DataType dataType) {
        setAttribute(ATTRIBUTE_DATA_TYPE, dataType);

    }

    /**
     * Get the 'session-id' attribute. If none is set, and RandomUUID is created.
     *
     * @return The 'session-id' attribute value
     */
    public UUID getSessionId() {
        synchronized (wrappedSession) {
            if (!wrappedSession.containsAttribute(ATTRIBUTE_SESSION_ID)) {
                wrappedSession.setAttribute(ATTRIBUTE_SESSION_ID, UUID.randomUUID());
            }

            return (UUID) wrappedSession.getAttribute(ATTRIBUTE_SESSION_ID);
        }
    }

    /**
     * Get the login time
     *
     * @return The login time
     */
    public Date getLoginTime() {
        return (Date) getAttribute(ATTRIBUTE_LOGIN_TIME);
    }

    /**
     * Get the last time the session has been accessed
     *
     * @return The last access time
     */
    public Date getLastAccessTime() {
        return new Date((Long) getAttribute(ATTRIBUTE_LAST_ACCESS_TIME));
    }

    /**
     * Get an ordered array of peer certificates, with the peer's own certificate first followed
     * by any certificate authorities.
     *
     * @return The client certificates
     */
    public Certificate[] getClientCertificates() {
        if (getFilterChain().contains(SslFilter.class)) {
            SslFilter sslFilter = (SslFilter) getFilterChain().get(SslFilter.class);

            SSLSession sslSession = SSLSession.class.cast(getAttribute(SslFilter.SSL_SECURED));

            if (sslSession != null) {
                try {
                    return sslSession.getPeerCertificates();
                } catch (SSLPeerUnverifiedException e) {
                    // ignore, certificate will not be available to the session
                }
            }

        }

        // no certificates available
        return null;

    }

    /**
     * Update the last-access-time session attribute with the current date
     */
    public void updateLastAccessTime() {
        setAttribute(ATTRIBUTE_LAST_ACCESS_TIME, System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    public Object getCurrentWriteMessage() {
        return wrappedSession.getCurrentWriteMessage();
    }

    /**
     * {@inheritDoc}
     */
    public WriteRequest getCurrentWriteRequest() {
        return wrappedSession.getCurrentWriteRequest();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBothIdle() {
        return wrappedSession.isBothIdle();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReaderIdle() {
        return wrappedSession.isReaderIdle();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWriterIdle() {
        return wrappedSession.isWriterIdle();
    }

    /**
     * Indicates whether the control socket for this session is secure, that is,
     * running over SSL/TLS
     *
     * @return <code>true</code> if the control socket is secured
     */
    public boolean isSecure() {
        return getFilterChain().contains(SslFilter.class);
    }

    /**
     * Increase the number of bytes written on the data connection
     *
     * @param increment The number of bytes written
     */
    public void increaseWrittenDataBytes(int increment) {
        if (wrappedSession instanceof AbstractIoSession) {
            ((AbstractIoSession) wrappedSession).increaseScheduledWriteBytes(increment);
            ((AbstractIoSession) wrappedSession).increaseWrittenBytes(
                    increment, System.currentTimeMillis());
        }
    }

    /**
     * Increase the number of bytes read on the data connection
     *
     * @param increment The number of bytes written
     */
    public void increaseReadDataBytes(int increment) {
        if (wrappedSession instanceof AbstractIoSession) {
            ((AbstractIoSession) wrappedSession).increaseReadBytes(increment,
                    System.currentTimeMillis());
        }
    }

    /**
     * Returns the last reply that was sent to the client.
     *
     * @return the last reply that was sent to the client.
     */
    public FtpReply getLastReply() {
        return lastReply;
    }

    /**
     * {@inheritDoc}
     */
    public WriteRequestQueue getWriteRequestQueue() {
        return wrappedSession.getWriteRequestQueue();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReadSuspended() {
        return wrappedSession.isReadSuspended();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWriteSuspended() {
        return wrappedSession.isWriteSuspended();
    }

    /**
     * {@inheritDoc}
     */
    public void setCurrentWriteRequest(WriteRequest currentWriteRequest) {
        wrappedSession.setCurrentWriteRequest(currentWriteRequest);
    }

    /**
     * {@inheritDoc}
     */
    public void updateThroughput(long currentTime, boolean force) {
        wrappedSession.updateThroughput(currentTime, force);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSecured() {
        return getFilterChain().contains(SslFilter.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isServer() {
        return (getService() instanceof IoAcceptor);
    }
}
