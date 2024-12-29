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

package org.apache.ftpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import org.apache.ftpserver.util.IoUtils;

/**
 * Provides the version of this release of FtpServer
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class Version {

    /**
     * Get the version of this FtpServer
     * @return The current version
     */
    public static String getVersion() {
        Properties props = new Properties();
        InputStream in = null;

        try {
            in = Version.class.getClassLoader().getResourceAsStream("org/apache/ftpserver/ftpserver.properties");
            props.load(in);
            return props.getProperty("ftpserver.version");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read version", e);
        } finally {
            IoUtils.close(in);
        }
    }
}
