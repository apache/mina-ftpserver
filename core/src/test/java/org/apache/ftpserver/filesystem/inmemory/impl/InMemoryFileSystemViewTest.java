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
import org.apache.ftpserver.usermanager.impl.BaseUser;

public class InMemoryFileSystemViewTest extends TestCase {

    private final String HOME_DIR = "/";
    protected BaseUser user = new BaseUser();

    public void testShouldNotCreateViewIfUserIsNull() {
        try {
            new InMemoryFileSystemView(null);
            fail("user should not be null");
        } catch (Exception ex) {
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }

    public void testShouldNotCreateViewIfUserIsHasNoHomeDir() {
        try {
            user.setHomeDirectory(null);
            new InMemoryFileSystemView(user);
            fail("user home directory should not be ull");
        } catch (Exception ex) {
            assertTrue(ex instanceof IllegalArgumentException);
        }
    }

    public void testHomeDir() throws Exception {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        assertEquals(HOME_DIR, view.getHomeDirectory().getAbsolutePath());
    }

    public void testWorkingDir() throws Exception {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        assertEquals(HOME_DIR, view.getWorkingDirectory().getAbsolutePath());
    }

    public void testChangeWorkingDir() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        view.getFile("1").mkdir();

        view.changeWorkingDirectory("1");

        assertEquals("1", view.getWorkingDirectory().getName());
    }

    public void testChangeWorkingDirToGoUp() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        view.getFile("1").mkdir();

        view.changeWorkingDirectory("1");

        assertEquals("1", view.getWorkingDirectory().getName());

        view.changeWorkingDirectory("..");
        assertEquals("/", view.getWorkingDirectory().getAbsolutePath());
    }

    public void testChangeWorkingDirToGoUp2Times() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        view.getFile("1/2").mkdir();

        view.changeWorkingDirectory("1/2");

        assertEquals("2", view.getWorkingDirectory().getName());

        assertEquals("/", view.getFile("../..").getAbsolutePath());
    }

    public void testGetCurrentDir() {
        InMemoryFileSystemView view = getInMemoryFileSystemView();
        assertEquals("/", view.getFile(".").getAbsolutePath());
    }

    private InMemoryFileSystemView getInMemoryFileSystemView() {
        user.setHomeDirectory(HOME_DIR);
        InMemoryFileSystemView view = new InMemoryFileSystemView(user);
        return view;
    }


}
