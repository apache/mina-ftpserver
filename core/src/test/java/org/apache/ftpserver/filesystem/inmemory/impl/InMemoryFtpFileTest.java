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

package org.apache.ftpserver.filesystem.inmemory.impl;

import junit.framework.TestCase;
import org.apache.ftpserver.filesystem.inmemoryfs.impl.InMemoryFileSystemView;
import org.apache.ftpserver.filesystem.inmemoryfs.impl.InMemoryFtpFile;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.usermanager.impl.BaseUser;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class InMemoryFtpFileTest extends TestCase {

    public static final String FILE1_NAME = "1";
    public static final String FILE2_NAME = "2";
    public static final String FILE3_NAME = "3";
    protected BaseUser user = new BaseUser();
    private final static String HOME_DIR = "/";

    public void testGetAbsolutePath() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file1 = view.getFile(FILE1_NAME);
        FtpFile file2 = view.getFile(FILE1_NAME + "/" + FILE2_NAME);
        assertEquals(HOME_DIR + FILE1_NAME, file1.getAbsolutePath());
        assertEquals(HOME_DIR + FILE1_NAME + "/" + FILE2_NAME, file2.getAbsolutePath());
    }

    public void testFindFile() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();

        InMemoryFtpFile file = (InMemoryFtpFile) view.getFile(FILE1_NAME);
        file.mkdir();

        InMemoryFtpFile subFile1 = (InMemoryFtpFile) view.getFile(file.getAbsolutePath() + "/" + FILE2_NAME);
        subFile1.mkdir();

        assertEquals(file, file.find(""));
        assertEquals(subFile1, file.find(subFile1.getName()));
    }

    public void testGetName() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file = view.getFile(FILE1_NAME);

        assertEquals(FILE1_NAME, file.getName());
    }

    public void testIsDirectory() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file = view.getFile(FILE1_NAME);

        assertTrue(file.isDirectory());
        assertFalse(file.isFile());
    }

    public void testIsFile() throws Exception{
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file = view.getFile(FILE1_NAME);
        file.createOutputStream(0).close(); //setting that file is file, not directory
        assertFalse(file.isDirectory());
        assertTrue(file.isFile());
    }
    public void testModificationTime() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file1 = view.getFile(FILE1_NAME);

        long time = System.currentTimeMillis();

        file1.setLastModified(time);

        assertEquals(time, file1.getLastModified());
    }

    public void testGetSize() throws Exception {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file = view.getFile(FILE1_NAME);

        OutputStream outputStream = file.createOutputStream(0);
        String data = "Test";
        outputStream.write(data.getBytes());
        outputStream.close();

        assertEquals(data.getBytes().length, file.getSize());
    }

    public void testDeleteLeaf() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file1 = view.getFile(FILE1_NAME);
        view.changeWorkingDirectory(FILE1_NAME);
        FtpFile file2 = view.getFile(FILE1_NAME + "/" + FILE2_NAME);
        file2.mkdir();

        assertTrue(file2.delete());

        assertEquals(1, view.getHomeDirectory().listFiles().size());
        assertEquals(0, file1.listFiles().size());
    }

    public void testDeleteRoot() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file1 = view.getFile(FILE1_NAME);
        file1.mkdir();
        view.changeWorkingDirectory(FILE1_NAME);

        assertTrue(file1.delete());

        assertEquals(0, view.getHomeDirectory().listFiles().size());
    }

    public void testWriteAndRead() throws Exception {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file = view.getFile(FILE1_NAME);

        OutputStream outputStream = file.createOutputStream(0);
        String input_data = "Hello, World!";
        outputStream.write(input_data.getBytes());
        outputStream.close();

        InputStream inputStream = file.createInputStream(0);
        StringBuilder output_data = new StringBuilder();
        int data;
        while ((data = inputStream.read()) != -1) {
            output_data.append((char) data);
        }
        inputStream.close();

        assertEquals(input_data, output_data.toString());
        assertEquals(1, view.getHomeDirectory().listFiles().size());

    }

    public void testMove() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file1 = view.getFile(FILE1_NAME);
        FtpFile file2 = view.getFile(FILE2_NAME);

        file1.move(file2);

        assertEquals(file1.getAbsolutePath(), file2.getAbsolutePath());
        assertEquals(file1.getName(), file2.getName());
        assertEquals(file1.getSize(), file2.getSize());
    }

    public void testListFiles() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file1 = view.getFile(FILE1_NAME);
        file1.mkdir();
        FtpFile file2 = view.getFile(FILE1_NAME + "/" + FILE2_NAME);
        file2.mkdir();

        List<FtpFile> result = (List<FtpFile>) file1.listFiles();
        assertEquals(1, result.size());
        assertEquals(file2, result.get(0));
    }

    public void testDeleteWithFilesInside() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file1 = view.getFile(FILE1_NAME);
        file1.mkdir();

        FtpFile file2 = view.getFile(FILE1_NAME + "/" + FILE2_NAME);
        file2.mkdir();

        file1.delete();
        assertEquals(0, view.getHomeDirectory().listFiles().size());
    }

    public void testMultipleWorkingDirectoryChange() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        FtpFile file1 = view.getFile(FILE1_NAME);
        file1.mkdir();

        assertEquals(1, view.getHomeDirectory().listFiles().size());

        FtpFile file2 = view.getFile(FILE1_NAME + "/" + FILE2_NAME);
        file2.mkdir();

        assertEquals(1, view.getHomeDirectory().listFiles().size());

        view.changeWorkingDirectory(FILE1_NAME);

        assertEquals(1, view.getHomeDirectory().listFiles().size());

        view.changeWorkingDirectory(FILE2_NAME);

        assertEquals(1, view.getHomeDirectory().listFiles().size());

        view.changeWorkingDirectory("..");

        assertEquals(1, view.getHomeDirectory().listFiles().size());

        view.changeWorkingDirectory("..");

        assertEquals(1, view.getHomeDirectory().listFiles().size());

        assertEquals(1, view.getHomeDirectory().listFiles().size());
        assertEquals(1, file1.listFiles().size());
        assertEquals(0, file2.listFiles().size());
    }

    public void testCreateDirectoryByMultiDirectoryPath() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        view.getFile(FILE1_NAME + "/" + FILE2_NAME).mkdir();

        assertEquals(1, view.getWorkingDirectory().listFiles().size());
        assertTrue(view.getFile(FILE1_NAME).doesExist() && view.getFile(FILE1_NAME).isDirectory());

        view.changeWorkingDirectory(FILE1_NAME);

        assertEquals(1, view.getWorkingDirectory().listFiles().size());
        assertTrue(view.getFile(FILE2_NAME).doesExist() && view.getFile(FILE2_NAME).isDirectory());

        view.changeWorkingDirectory(FILE2_NAME);

        assertEquals(0, view.getWorkingDirectory().listFiles().size());
    }

    public void testCWDToNotExisingDirectoryNotChangingIt() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        view.getFile(FILE1_NAME + "/" + FILE2_NAME + "/" + FILE3_NAME).mkdir();
        view.changeWorkingDirectory(FILE1_NAME + "/" + FILE2_NAME + "/" + FILE3_NAME);

        assertEquals(FILE3_NAME, view.getWorkingDirectory().getName());

        view.changeWorkingDirectory("not_exist");

        assertEquals(FILE3_NAME, view.getWorkingDirectory().getName());
    }

    public void testCreateDirectoryByMultiDirectoryAbsolutePath() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        view.getFile(HOME_DIR + "/" + FILE1_NAME + "/" + FILE2_NAME + "/" + FILE3_NAME).mkdir();

        assertEquals(1, view.getHomeDirectory().listFiles().size()); // home
        assertEquals(1, view.getHomeDirectory().listFiles().get(0).listFiles().size()); //1
        assertEquals(1, view.getHomeDirectory().listFiles().get(0).listFiles().get(0).listFiles().size()); //2
        assertEquals(0, view.getHomeDirectory().listFiles().get(0).listFiles().get(0).listFiles().get(0).listFiles().size()); //3
    }

    public void testGoUpDirectory() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        view.getFile(HOME_DIR + "/" + FILE1_NAME + "/" + FILE2_NAME + "/" + FILE3_NAME).mkdir();

        view.changeWorkingDirectory(HOME_DIR + "/" + FILE1_NAME + "/" + FILE2_NAME + "/" + FILE3_NAME);
        view.changeWorkingDirectory("..");
        assertEquals(FILE2_NAME, view.getWorkingDirectory().getName());
    }

    private InMemoryFileSystemView getInMemoryFileSystemView() {
        user.setHomeDirectory(HOME_DIR);
        return new InMemoryFileSystemView(user);
    }

}
