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

package org.apache.ftpserver.filesystem.inmemoryfs.impl;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View representation of the in-memory file system.
 * <p>
 * Provides mechanisms to navigate and manage the structure of the in-memory file system.
 * </p>
 *
 * <p>
 * This class is designed to support operations such as listing files, managing directories,
 * and accessing file properties within a non-persistent in-memory environment.
 * </p>
 */
public class InMemoryFileSystemView implements FileSystemView {

    public static final String PARENT_DIRECTORY = "..";
    public static final String CURRENT_DIRECTORY = ".";
    public static final String CURRENT = "./";
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryFileSystemView.class);
    private final InMemoryFtpFile homeDirectory;
    private InMemoryFtpFile workingDirectory;

    /**
     * Default constructor for the factory.
     * @param user The acting User
     */
    public InMemoryFileSystemView(User user) {
        validateUser(user);
        this.homeDirectory = InMemoryFtpFile.createRoot(user.getHomeDirectory());
        this.workingDirectory = homeDirectory;
    }

    /**
     * validates whether the user is eligible to create an instance
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }

        if (user.getHomeDirectory() == null) {
            throw new IllegalArgumentException("User home directory cannot be null");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FtpFile getHomeDirectory() {
        return homeDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FtpFile getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changeWorkingDirectory(String path) {
        LOG.debug("changing working directory from {} to path {}", workingDirectory.getAbsolutePath(), path);

        if (isPathNotMatchingCurrentWorkingDirPath(path)) {
            InMemoryFtpFile mayBeWorkingDirectory;

            if (isAbsolute(path)) {
                mayBeWorkingDirectory = homeDirectory.find(path);
            } else {
                mayBeWorkingDirectory = workingDirectory.find(path);
            }

            if (mayBeWorkingDirectory.doesExist() && mayBeWorkingDirectory.isDirectory()) {
                workingDirectory = mayBeWorkingDirectory;
                LOG.debug("changed working directory to {}", workingDirectory.getAbsolutePath());
                return true;
            }

            LOG.info("changing working directory failed - cannot find file by path {}", path);

            return false;
        }

        //path is same as current working directory
        return true;
    }

    /**
     *
     * @return true if path is absolute, otherwise false
     */
    private boolean isPathNotMatchingCurrentWorkingDirPath(String dir) {
        return !workingDirectory.getAbsolutePath().equals(dir);
    }

    /**
     * Retrieves a file or directory within the in-memory file system based on the given path.
     * <p>
     * The method resolves paths using the following rules:
     * <ul>
     *   <li>If the path starts with "../", it navigates to the parent directory.</li>
     *   <li>If the path starts with "./", it remains in the current directory.</li>
     *   <li>Otherwise, it searches for the file or directory in the current directory.</li>
     * </ul>
     * </p>
     *
     * @param path the path to the file or directory
     * @param file the current file or directory context
     * @return the resolved file or directory, or {@code null} if not found
     */
    private FtpFile getFile(String path, InMemoryFtpFile file) {
        path = removeSlash(path);

        if (path.startsWith(PARENT_DIRECTORY)) {
            if (path.equals(PARENT_DIRECTORY)) {
                return file.getParent();
            } else {
                if (file.getParent() == null) {
                    return null;
                }

                return getFile(path.substring(2), (InMemoryFtpFile) file.getParent());
            }
        } else if (path.startsWith(CURRENT_DIRECTORY)) {
            if (path.equals(CURRENT_DIRECTORY)) {
                return file;
            } else if (path.startsWith(CURRENT)) {
                if (path.equals(CURRENT)) {
                    return file;
                } else {
                    return getFile(path.substring(2), file);
                }
            }
        }

        return file.find(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FtpFile getFile(String path) {
        if (isAbsolute(path)) {
            return getFile(path, homeDirectory);
        } else {
            return getFile(path, workingDirectory);
        }
    }

    /**
     * check if path is absolute
     * @return true if it is, otherwise false
     */
    private boolean isAbsolute(String path) {
        return path.startsWith("/");
    }

    /**
     * Removes the leading slash ('/') from the given path, if present.
     * <p>
     * For example:
     * <ul>
     *   <li>If the input path is "/dir1/dir2", the returned path will be "dir1/dir2".</li>
     *   <li>If the input path is "./dir1/dir2", the returned path remains "./dir1/dir2".</li>
     * </ul>
     * </p>
     *
     * @param path the input path from which to remove the leading slash
     * @return the path without the leading slash if it starts with '/', otherwise the original path
     */
    private String removeSlash(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }

        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRandomAccessible() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
    }
}

