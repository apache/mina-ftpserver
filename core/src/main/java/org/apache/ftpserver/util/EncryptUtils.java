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

package org.apache.ftpserver.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.ftpserver.command.impl.MD5;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * String encryption utility methods.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class EncryptUtils {

    /**
     * Encrypt byte array.
     *
     * @param source The data to encrypt
     * @param algorithm The algorithm to use
     * @throws NoSuchAlgorithmException If th algorithm does not exist
     * @return The encrypted data
     */
    public static final byte[] encrypt(byte[] source, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        md.update(source);

        return md.digest();
    }

    /**
     * Encrypt string
     *
     * @param source The data to encrypt
     * @param algorithm The algorithm to use
     * @throws NoSuchAlgorithmException If th algorithm does not exist
     * @return The encrypted data
     */
    public static final String encrypt(String source, String algorithm) throws NoSuchAlgorithmException {
        if (source == null) {
            source = "";
        }

        byte[] resByteArray = encrypt(source.getBytes( StandardCharsets.UTF_8 ), algorithm);

        return StringUtils.toHexString(resByteArray);
    }

    /**
     * Encrypt string using MD5 algorithm
     *
     * @param source The data to encrypt
     * @return The encrypted data
     */
    public static final String encryptMD5(String source) {
        try {
            return encrypt(source, MD5.MD5);
        } catch (NoSuchAlgorithmException ex) {
            // this should never happen
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Encrypt string using SHA-1 algorithm
     *
     * @param source The data to encrypt
     * @return The encrypted data
     */
    public static final String encryptSHA(String source) {
        try {
            return encrypt(source, "SHA");
        } catch (NoSuchAlgorithmException ex) {
            // this should never happen
            throw new IllegalArgumentException(ex);
        }
    }


    /**
     * Encrypt string using SHA-256 algorithm
     *
     * @param source The data to encrypt
     * @return The encrypted data
     */
    public static final String encryptSHA256(String source) {
        try {
            return encrypt(source, "SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            // this should never happen
            throw new RuntimeException(ex);
        }
    }


    /**
     * Encrypt string using SHA-512 algorithm
     *
     * @param source The data to encrypt
     * @return The encrypted data
     */
    public static final String encryptSHA512(String source) {
        try {
            return encrypt(source, "SHA-512");
        } catch (NoSuchAlgorithmException ex) {
            // this should never happen
            throw new RuntimeException(ex);
        }
    }
}
