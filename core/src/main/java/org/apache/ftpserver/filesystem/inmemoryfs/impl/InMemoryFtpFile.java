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

import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryFtpFile implements FtpFile {

    public static final String SLASH = "/";
    private static final Logger log = LoggerFactory.getLogger(InMemoryFtpFile.class);
    private boolean isExist = false;
    private boolean isFolder = true;
    private String name;
    private Map<String, InMemoryFtpFile> children;
    private InMemoryFtpFile parent;
    private long lastModify = System.currentTimeMillis();

    private ByteArrayOutputStream data = new ByteArrayOutputStream();

    private InMemoryFtpFile(String name, InMemoryFtpFile parent) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("null or empty file name");
        }

        this.name = name;
        this.children = new ConcurrentHashMap<>();
        this.parent = parent;
        log.debug("created file in path {}", this.getAbsolutePath());
    }

    static InMemoryFtpFile createRoot(String name) {
        InMemoryFtpFile file = new InMemoryFtpFile(name, null);
        file.isExist = true;
        file.isFolder = true;
        return file;
    }

    private static InMemoryFtpFile createLeaf(String name, InMemoryFtpFile parent) {
        Objects.requireNonNull(parent);
        return new InMemoryFtpFile(name, parent);
    }

    @Override
    public String getAbsolutePath() {
        if (isRoot()) {
            return name;
        }
        return parent.getAbsolutePath(name);
    }

    private String getAbsolutePath(String childPath) {
        if (isRoot()) {
            return getName() + childPath;
        }
        return parent.getAbsolutePath(name + SLASH + childPath);
    }

    private boolean isRoot() {
        return parent == null;
    }

    protected List<InMemoryFtpFile> getChildren() {
        return children.values().stream().filter(InMemoryFtpFile::doesExist).collect(Collectors.toList());
    }

    public InMemoryFtpFile find(String path) {
        if (path.startsWith(SLASH)) {
            path = path.substring(1);
        }

        if (path.isEmpty()) {
            return this;
        }

        if (isGoUpPath(path)) {
            if (isRoot()) {
                return this;
            }
            return parent.find(path.substring(2));
        }

        String[] splitPath = path.split(SLASH, 2);

        if (isMultiFolderPath(splitPath)) {
            InMemoryFtpFile file = this.find(splitPath[0]);
            return file.find(splitPath[1]);
        } else {
            InMemoryFtpFile leaf = InMemoryFtpFile.createLeaf(path, this);
            InMemoryFtpFile existingFile = children.putIfAbsent(leaf.getName(), leaf);
            return existingFile == null ? leaf : existingFile;
        }
    }

    private boolean isMultiFolderPath(String[] splitPath) {
        return splitPath.length > 1;
    }

    private boolean isGoUpPath(String path) {
        return path.startsWith("..");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return isRoot() || isFolder;
    }

    @Override
    public boolean isFile() {
        return !isRoot() && !isFolder;
    }

    @Override
    public boolean doesExist() {
        return isExist;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public String getOwnerName() {
        return null;
    }

    @Override
    public String getGroupName() {
        return null;
    }

    @Override
    public int getLinkCount() {
        return 0;
    }

    @Override
    public long getLastModified() {
        return lastModify;
    }

    @Override
    public boolean setLastModified(long time) {
        lastModify = time;
        return true;
    }

    @Override
    public long getSize() {
        return data.size();
    }

    @Override
    public Object getPhysicalFile() {
        throw new UnsupportedOperationException("physical file not available in memory file system");
    }

    @Override
    public boolean mkdir() {
        log.debug("creating folder at path {}", getAbsolutePath());
        if (isExist && isFolder) {
            return false;
        }
        if (!isRoot()) {
            isExist = true;
            isFolder = true;
            parent.mkdir();
        }
        return true;
    }

    @Override
    public boolean delete() {
        log.debug("deleting file at path {}", getAbsolutePath());
        isExist = false;
        this.children = null;
        removeFromParent();
        this.parent = null;
        return true;
    }

    private void removeFromParent() {
        parent.children.remove(getName());
    }

    @Override
    public boolean move(FtpFile destination) {
        log.debug("moving file from {} to {}", getAbsolutePath(), destination.getAbsolutePath());
        removeFromParent();
        InMemoryFtpFile inMemoryDestination = (InMemoryFtpFile) destination;
        clone(inMemoryDestination);
        inMemoryDestination.parent.getChildren().add(inMemoryDestination);
        return true;
    }

    private void clone(InMemoryFtpFile target) {
        target.data = this.data;
        target.isFolder = this.isFolder;
        target.children = this.children;
        target.isExist = this.isExist;
        target.parent = this.parent;
        target.name = this.name;
    }

    @Override
    public List<? extends FtpFile> listFiles() {
        if (isDirectory()) {
            return children.values().stream().filter(InMemoryFtpFile::doesExist).collect(Collectors.toList());
        }

        return null;
    }

    @Override
    public OutputStream createOutputStream(long offset) {
        isFolder = false;
        isExist = true;
        if (offset != 0) {
            throw new IllegalArgumentException("offset must be equal 0");
        }
        return new ByteArrayOutputStreamWrapper(data);
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        validateInputStream(offset);
        return new ByteArrayInputStream(data.toByteArray());
    }

    private void validateInputStream(long offset) throws IOException {
        if (isFolder) {
            throw new IOException("cannot read bytes from folder");
        }
        if (offset != 0) {
            throw new IllegalArgumentException("offset must be equal 0");
        }
    }

    public FtpFile getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InMemoryFtpFile file = (InMemoryFtpFile) o;
        return Objects.equals(getAbsolutePath(), file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAbsolutePath());
    }

    private static class ByteArrayOutputStreamWrapper extends OutputStream {

        private final ByteArrayOutputStream byteArrayOutputStream;

        ByteArrayOutputStreamWrapper(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
        }

        @Override
        public void write(int b) {
            byteArrayOutputStream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            byteArrayOutputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            byteArrayOutputStream.write(b, off, len);
        }
    }
}
