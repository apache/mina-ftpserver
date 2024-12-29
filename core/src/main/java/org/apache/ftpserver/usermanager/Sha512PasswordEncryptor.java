
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

package org.apache.ftpserver.usermanager;

import org.apache.ftpserver.util.EncryptUtils;
import org.apache.ftpserver.util.PasswordUtil;

/**
 * Password encryptor that hashes the password using SHA-512.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class Sha512PasswordEncryptor implements PasswordEncryptor {

    /**
     * Hashes the password using SHA-512
     * @param password The password to encrypt
     * @return The encrypted password
     */
    public String encrypt(String password) {
        return EncryptUtils.encryptSHA512(password);
    }

    /**
     * {@inheritDoc}
     */
    public boolean matches(String passwordToCheck, String storedPassword) {
        if (storedPassword == null) {
            throw new NullPointerException("storedPassword can not be null");
        }
        if (passwordToCheck == null) {
            throw new NullPointerException("passwordToCheck can not be null");
        }

        return PasswordUtil.secureCompareFast(encrypt(passwordToCheck).toLowerCase(), storedPassword.toLowerCase());
    }
}
