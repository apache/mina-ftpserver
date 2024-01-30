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

public class InMemoryFileSystemView implements FileSystemView {

    public static final String PARENT_DIRECTORY = "..";
    public static final String CURRENT_DIRECTORY = ".";
    public static final String CURRENT = "./";
    private static final Logger log = LoggerFactory.getLogger(InMemoryFileSystemView.class);
    private final InMemoryFtpFile homeDirectory;
    private InMemoryFtpFile workingDirectory;

    public InMemoryFileSystemView(User user) {
        validateUser(user);
        this.homeDirectory = InMemoryFtpFile.createRoot(user.getHomeDirectory());
        this.workingDirectory = homeDirectory;
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        if (user.getHomeDirectory() == null) {
            throw new IllegalArgumentException("User home directory cannot be null");
        }
    }

    @Override
    public FtpFile getHomeDirectory() {
        return homeDirectory;
    }

    @Override
    public FtpFile getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public boolean changeWorkingDirectory(String path) {
        log.debug("changing working directory from {} to path {}", workingDirectory.getAbsolutePath(), path);
        if (isPathNotMatchingCurrentWorkingDirPath(path)) {
            InMemoryFtpFile mayBeWorkingDirectory;
            if (isAbsolute(path)) {
                mayBeWorkingDirectory = homeDirectory.find(path);
            } else {
                mayBeWorkingDirectory = workingDirectory.find(path);
            }

            if (mayBeWorkingDirectory.doesExist() && mayBeWorkingDirectory.isDirectory()) {
                workingDirectory = mayBeWorkingDirectory;
                log.debug("changed working directory to {}", workingDirectory.getAbsolutePath());
                return true;
            }
            log.info("changing working directory failed - cannot find file by path {}", path);
            return false;
        }
        //path is same as current working directory
        return true;
    }

    private boolean isPathNotMatchingCurrentWorkingDirPath(String dir) {
        return !workingDirectory.getAbsolutePath().equals(dir);
    }

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

    @Override
    public FtpFile getFile(String path) {
        if (isAbsolute(path)) {
            return getFile(path, homeDirectory);
        } else {
            return getFile(path, workingDirectory);
        }
    }

    private boolean isAbsolute(String path) {
        return path.startsWith("/");
    }

    private String removeSlash(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    @Override
    public boolean isRandomAccessible() {
        return false;
    }

    @Override
    public void dispose() {
    }
}
