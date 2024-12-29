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

    /**
     * Contains user name between USER and PASS commands
     */
    public static final String ATTRIBUTE_PREFIX = "org.apache.ftpserver.";
    private static final String ATTRIBUTE_USER_ARGUMENT = ATTRIBUTE_PREFIX
            + "user-argument";
    private static final String ATTRIBUTE_SESSION_ID = ATTRIBUTE_PREFIX
            + "session-id";
    private static final String ATTRIBUTE_USER = ATTRIBUTE_PREFIX + "user";
    private static final String ATTRIBUTE_LANGUAGE = ATTRIBUTE_PREFIX
            + "language";
    private static final String ATTRIBUTE_LOGIN_TIME = ATTRIBUTE_PREFIX
            + "login-time";
    private static final String ATTRIBUTE_DATA_CONNECTION = ATTRIBUTE_PREFIX
            + "data-connection";
    private static final String ATTRIBUTE_FILE_SYSTEM = ATTRIBUTE_PREFIX
            + "file-system";
    private static final String ATTRIBUTE_RENAME_FROM = ATTRIBUTE_PREFIX
            + "rename-from";
    private static final String ATTRIBUTE_FILE_OFFSET = ATTRIBUTE_PREFIX
            + "file-offset";
    private static final String ATTRIBUTE_DATA_TYPE = ATTRIBUTE_PREFIX
            + "data-type";
    private static final String ATTRIBUTE_STRUCTURE = ATTRIBUTE_PREFIX
            + "structure";
    private static final String ATTRIBUTE_FAILED_LOGINS = ATTRIBUTE_PREFIX
            + "failed-logins";
    private static final String ATTRIBUTE_LISTENER = ATTRIBUTE_PREFIX
            + "listener";
    private static final String ATTRIBUTE_MAX_IDLE_TIME = ATTRIBUTE_PREFIX
            + "max-idle-time";
    private static final String ATTRIBUTE_LAST_ACCESS_TIME = ATTRIBUTE_PREFIX
            + "last-access-time";
    private static final String ATTRIBUTE_CACHED_REMOTE_ADDRESS = ATTRIBUTE_PREFIX
            + "cached-remote-address";
    private final IoSession wrappedSession;
    private final FtpServerContext context;
    /**
     * Last reply that was sent to the client, if any.
     */
    private FtpReply lastReply = null;

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
    public void resetState() {
        removeAttribute(ATTRIBUTE_RENAME_FROM);
        removeAttribute(ATTRIBUTE_FILE_OFFSET);
    }

    public synchronized ServerDataConnectionFactory getDataConnection() {
        if (containsAttribute(ATTRIBUTE_DATA_CONNECTION)) {
            return (ServerDataConnectionFactory) getAttribute(ATTRIBUTE_DATA_CONNECTION);
        } else {
            IODataConnectionFactory dataCon = new IODataConnectionFactory(
                    context, this);
            dataCon.setServerControlAddress(((InetSocketAddress) getLocalAddress()).getAddress());
            setAttribute(ATTRIBUTE_DATA_CONNECTION, dataCon);

            return dataCon;
        }
    }

    public FileSystemView getFileSystemView() {
        return (FileSystemView) getAttribute(ATTRIBUTE_FILE_SYSTEM);
    }

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

    public Listener getListener() {
        return (Listener) getAttribute(ATTRIBUTE_LISTENER);
    }

    public void setListener(Listener listener) {
        setAttribute(ATTRIBUTE_LISTENER, listener);
    }

    public FtpSession getFtpletSession() {
        return new DefaultFtpSession(this);
    }

    public String getLanguage() {
        return (String) getAttribute(ATTRIBUTE_LANGUAGE);
    }

    public void setLanguage(String language) {
        setAttribute(ATTRIBUTE_LANGUAGE, language);

    }

    public String getUserArgument() {
        return (String) getAttribute(ATTRIBUTE_USER_ARGUMENT);
    }

    public void setUser(User user) {
        setAttribute(ATTRIBUTE_USER, user);

    }

    public void setUserArgument(String userArgument) {
        setAttribute(ATTRIBUTE_USER_ARGUMENT, userArgument);

    }

    public int getMaxIdleTime() {
        return (Integer) getAttribute(ATTRIBUTE_MAX_IDLE_TIME, 0);
    }

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

    public synchronized void increaseFailedLogins() {
        int failedLogins = (Integer) getAttribute(ATTRIBUTE_FAILED_LOGINS, 0);
        failedLogins++;
        setAttribute(ATTRIBUTE_FAILED_LOGINS, failedLogins);
    }

    public int getFailedLogins() {
        return (Integer) getAttribute(ATTRIBUTE_FAILED_LOGINS, 0);
    }

    public void setLogin(FileSystemView fsview) {
        setAttribute(ATTRIBUTE_LOGIN_TIME, new Date());
        setAttribute(ATTRIBUTE_FILE_SYSTEM, fsview);
    }

    public void reinitialize() {
        logoutUser();
        removeAttribute(ATTRIBUTE_USER);
        removeAttribute(ATTRIBUTE_USER_ARGUMENT);
        removeAttribute(ATTRIBUTE_LOGIN_TIME);
        removeAttribute(ATTRIBUTE_FILE_SYSTEM);
        removeAttribute(ATTRIBUTE_RENAME_FROM);
        removeAttribute(ATTRIBUTE_FILE_OFFSET);
    }

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

    public void setFileOffset(long fileOffset) {
        setAttribute(ATTRIBUTE_FILE_OFFSET, fileOffset);

    }

    public void setRenameFrom(FtpFile renFr) {
        setAttribute(ATTRIBUTE_RENAME_FROM, renFr);

    }

    public FtpFile getRenameFrom() {
        return (FtpFile) getAttribute(ATTRIBUTE_RENAME_FROM);
    }

    public long getFileOffset() {
        return (Long) getAttribute(ATTRIBUTE_FILE_OFFSET, 0L);
    }

    public void setStructure(Structure structure) {
        setAttribute(ATTRIBUTE_STRUCTURE, structure);
    }

    public void setDataType(DataType dataType) {
        setAttribute(ATTRIBUTE_DATA_TYPE, dataType);

    }

    /**
     * {@inheritDoc}
     */
    public UUID getSessionId() {
        synchronized (wrappedSession) {
            if (!wrappedSession.containsAttribute(ATTRIBUTE_SESSION_ID)) {
                wrappedSession.setAttribute(ATTRIBUTE_SESSION_ID, UUID.randomUUID());
            }
            return (UUID) wrappedSession.getAttribute(ATTRIBUTE_SESSION_ID);
        }
    }

    public FtpIoSession(IoSession wrappedSession, FtpServerContext context) {
        this.wrappedSession = wrappedSession;
        this.context = context;
    }

    public Structure getStructure() {
        return (Structure) getAttribute(ATTRIBUTE_STRUCTURE, Structure.FILE);
    }

    public DataType getDataType() {
        return (DataType) getAttribute(ATTRIBUTE_DATA_TYPE, DataType.ASCII);
    }

    public Date getLoginTime() {
        return (Date) getAttribute(ATTRIBUTE_LOGIN_TIME);
    }

    public Date getLastAccessTime() {
        return (Date) getAttribute(ATTRIBUTE_LAST_ACCESS_TIME);
    }

    public Certificate[] getClientCertificates() {
        if (getFilterChain().contains(SslFilter.class)) {
            SslFilter sslFilter = (SslFilter) getFilterChain().get(
                    SslFilter.class);

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

    public void updateLastAccessTime() {
        setAttribute(ATTRIBUTE_LAST_ACCESS_TIME, new Date());
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
     * @return true if the control socket is secured
     */
    public boolean isSecure() {
        return getFilterChain().contains(SslFilter.class);
    }

    /**
     * Increase the number of bytes written on the data connection
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

    public boolean isSecured() {
        return getFilterChain().contains(SslFilter.class);
    }

    @Override
    public boolean isServer() {
        return (getService() instanceof IoAcceptor);
    }
}
