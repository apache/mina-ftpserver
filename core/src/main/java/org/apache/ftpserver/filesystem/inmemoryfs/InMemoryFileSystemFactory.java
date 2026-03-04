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

package org.apache.ftpserver.filesystem.inmemoryfs;

import org.apache.ftpserver.filesystem.inmemoryfs.impl.InMemoryFileSystemView;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for creating instances of an in-memory file system.
 * <p>
 *  * This implementation provides a lightweight and non-persistent file system
 *  * stored entirely in memory, suitable for testing or temporary data storage.
 * </p>
 */
public class InMemoryFileSystemFactory implements FileSystemFactory {

    private static final Map<String, InMemoryFileSystemView> filesystemMap = new ConcurrentHashMap<>();

    /**
     * Create the appropriate user file system view.
     * {@inheritDoc}
     */
    @Override
    public synchronized FileSystemView createFileSystemView(User user) throws FtpException {
        Objects.requireNonNull(user);

        if (filesystemMap.containsKey(user.getName())) {
            return filesystemMap.get(user.getName());
        }

        InMemoryFileSystemView view = new InMemoryFileSystemView(user);
        filesystemMap.put(user.getName(), view);

        return view;
    }
}
