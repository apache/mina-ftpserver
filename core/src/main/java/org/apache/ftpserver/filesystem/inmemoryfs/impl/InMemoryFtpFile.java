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

/**
 * This class wraps in-memory file object.
 */
public class InMemoryFtpFile implements FtpFile {

    public static final String SLASH = "/";
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryFtpFile.class);
    private boolean isExist = false;
    private boolean isFolder = true;
    private String name;
    private Map<String, InMemoryFtpFile> children;
    private InMemoryFtpFile parent;
    private long lastModify = System.currentTimeMillis();

    private ByteArrayOutputStream data = new ByteArrayOutputStream();

    /**
     * default constructor
     * @param name filename
     * @param parent file, null if file is root
     * @throws IllegalArgumentException 'name' is empty or null
     */
    private InMemoryFtpFile(String name, InMemoryFtpFile parent) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("null or empty file name");
        }

        this.name = name;
        this.children = new ConcurrentHashMap<>();
        this.parent = parent;
        LOG.debug("created file in path {}", this.getAbsolutePath());
    }

    /**
     * Creates a root file object for the in-memory file system.
     *
     * @param name The name of the root directory.
     * @return The created root file object.
     */
    static InMemoryFtpFile createRoot(String name) {
        InMemoryFtpFile file = new InMemoryFtpFile(name, null);
        file.isExist = true;
        file.isFolder = true;

        return file;
    }

    /**
     * Creates a leaf file object within the given parent directory.
     *
     * @param name The name of the leaf file.
     * @param parent The parent directory for the leaf file.
     * @return The created leaf file object.
     * @throws NullPointerException If the `parent` parameter is null.
     * @throws IllegalArgumentException  If the `name` parameter is null or empty.
     */
    private static InMemoryFtpFile createLeaf(String name, InMemoryFtpFile parent) {
        Objects.requireNonNull(parent);

        return new InMemoryFtpFile(name, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAbsolutePath() {
        if (isRoot()) {
            return name;
        }

        return parent.getAbsolutePath(name);
    }

    /**
     * Returns the absolute path of the given child path within this directory.
     * This method is used internally to construct the absolute path for child files or directories.
     *
     * @param childPath The relative path of the child.
     * @return The absolute path of the child.
     */
    private String getAbsolutePath(String childPath) {
        if (isRoot()) {
            return getName() + childPath;
        }

        return parent.getAbsolutePath(name + SLASH + childPath);
    }

    /**
     * Check is file root file.
     * @return <code>true</code> if file is root
     */
    private boolean isRoot() {
        return parent == null;
    }

    /**
     * This method returns a list of all child files and directories that currently exist within this directory.
     *
     * @return A list of child files and directories.
     */
    protected List<InMemoryFtpFile> getChildren() {
        return children.values().stream().filter(InMemoryFtpFile::doesExist).collect(Collectors.toList());
    }

    /**
     * This method searches for the specified file or directory within the current directory and its subdirectories.
     * It supports relative paths, including ".." for navigating to the parent directory.
     *
     * @param path The relative path of the file or directory to find.
     * @return The found file object, or null if the file or directory does not exist.
     */
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

    /**
     * Checks if the given path represents a multi-level directory path.
     *
     * A multi-level directory path contains more than one directory level
     * (e.g., "dir1/dir2/file.txt").
     *
     * @param splitPath An array of path components obtained by splitting the original path.
     * @return `true` if the path contains more than one directory level, `false` otherwise.
     */
    private boolean isMultiFolderPath(String[] splitPath) {
        return splitPath.length > 1;
    }

    /**
     * Checks if the given path represents a request to navigate to the parent directory.
     *
     * @param path The path to be checked.
     * @return `true` if the path starts with ".." (indicating a parent directory request), `false` otherwise.
     */
    private boolean isGoUpPath(String path) {
        return path.startsWith("..");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return isRoot() || isFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFile() {
        return !isRoot() && !isFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesExist() {
        return isExist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWritable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRemovable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOwnerName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLinkCount() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastModified() {
        return lastModify;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setLastModified(long time) {
        lastModify = time;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize() {
        return data.size();
    }

    /**
     * There is no physicl file in in-memory filesystem.
     * @throws UnsupportedOperationException
     */
    @Override
    public Object getPhysicalFile() {
        throw new UnsupportedOperationException("physical file not available in memory file system");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdir() {
        LOG.debug("creating folder at path {}", getAbsolutePath());

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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete() {
        LOG.debug("deleting file at path {}", getAbsolutePath());
        isExist = false;
        this.children = null;
        removeFromParent();
        this.parent = null;

        return true;
    }

    /**
     * This method removes the entry for this file or directory from the `children` map of its parent.
     */
    private void removeFromParent() {
        parent.children.remove(getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean move(FtpFile destination) {
        LOG.debug("moving file from {} to {}", getAbsolutePath(), destination.getAbsolutePath());
        removeFromParent();
        InMemoryFtpFile inMemoryDestination = (InMemoryFtpFile) destination;
        clone(inMemoryDestination);
        inMemoryDestination.parent.getChildren().add(inMemoryDestination);

        return true;
    }

    /**
     * Creates a shallow copy of this InMemoryFtpFile object into the provided target object.
     *
     * This method copies the data references, flags, and parent reference from the current object to the target object.
     * It does not create deep copies of child objects or data.
     *
     * @param target The target InMemoryFtpFile object to copy the data into.
     */
    private void clone(InMemoryFtpFile target) {
        target.data = this.data;
        target.isFolder = this.isFolder;
        target.children = this.children;
        target.isExist = this.isExist;
        target.parent = this.parent;
        target.name = this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends FtpFile> listFiles() {
        if (isDirectory()) {
            return children.values().stream().filter(InMemoryFtpFile::doesExist).collect(Collectors.toList());
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream createOutputStream(long offset) {
        isFolder = false;
        isExist = true;
        if (offset != 0) {
            throw new IllegalArgumentException("offset must be equal 0");
        }

        return new ByteArrayOutputStreamWrapper(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream createInputStream(long offset) throws IOException {
        validateInputStream(offset);

        return new ByteArrayInputStream(data.toByteArray());
    }

   /**
     * This method checks if the current object is a directory and if the specified offset is valid.
     *
     * @param offset The desired offset for the input stream.
     * @throws IOException If the current object is a directory.
     * @throws IllegalArgumentException If the offset is not equal to 0.
     */
    private void validateInputStream(long offset) throws IOException {
        if (isFolder) {
            throw new IOException("cannot read bytes from folder");
        }

        if (offset != 0) {
            throw new IllegalArgumentException("offset must be equal 0");
        }
    }

    /**
     * Returns the parent directory of this file or directory.
     *
     * @return The parent directory object, or null if this is the root directory.
     */
    public FtpFile getParent() {
        return parent;
    }

    /**
     * Two InMemoryFtpFile objects are considered equal if they have the same absolute path.
     *
     * @param o The object to compare with.
     * @return `true` if the objects are equal, `false` otherwise.
     */
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

    /**
     * Returns the hash code of this InMemoryFtpFile object.
     *
     * The hash code is calculated based on the absolute path of the file or directory.
     *
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getAbsolutePath());
    }

    /**
     * This class allows the ByteArrayOutputStream to be used as an OutputStream
     * by delegating all write operations to the underlying ByteArrayOutputStream.
     */
    private static class ByteArrayOutputStreamWrapper extends OutputStream {

        private final ByteArrayOutputStream byteArrayOutputStream;

        /**
         * Default Constructor.
         */
        ByteArrayOutputStreamWrapper(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(int b) {
            byteArrayOutputStream.write(b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b) throws IOException {
            byteArrayOutputStream.write(b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b, int off, int len) {
            byteArrayOutputStream.write(b, off, len);
        }
    }
}

