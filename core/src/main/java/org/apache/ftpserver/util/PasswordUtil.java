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

public class PasswordUtil {
    /**
     * Securely compares two strings up to a maximum number of characters in a way
     * that obscures the password length from timing attacks
     * 
     * @param input
     *            user input
     * @param password
     *            correct password
     * @param loops
     *            number of characters to compare; must be larger than password
     *            length; 1024 is a good number
     * 
     * @throws IllegalArgumentException
     *             when the limit is less than the password length
     * 
     * @return true if the passwords match
     */
    public static boolean secureCompare(String input, String password, int loops) {
        if (loops < password.length()) {
            throw new IllegalArgumentException("loops must be equal or greater than the password length");
        }
        /*
         * Set the default result based on the string lengths; if the lengths do not
         * match then we know that this comparison should always fail.
         */
        int result = (input.length() ^ password.length());
        /*
         * Cycle through all of the characters up to the limit value
         * 
         * Important to note that this loop may return a false positive comparison if
         * the target string is a repeating set of characters in direct multiples of the
         * input string. This design fallacy is corrected by the original length
         * comparison above. The use of modulo this way is intended to prevent compiler
         * and memory optimizations for retrieving the same char position in sequence.
         */
        for (int i = 0; i < loops; i++) {
            result |= (input.charAt(i % input.length()) ^ password.charAt(i % password.length()));
        }

        return (result == 0);
    }
    
    /**
     * Securely compares two strings forcing the number of loops equal to password length
     * thereby obscuring the password length based on user input
     * 
     * @param input
     *            user input
     * @param password
     *            correct password
     * @throws IllegalArgumentException
     *             when the limit is less than the password length
     * 
     * @return true if the passwords match
     */
    public static boolean secureCompareFast(String input, String password) {
        /*
         * the number of compare loops
         */
        int loops = password.length();
        /*
         * Set the default result based on the string lengths; if the lengths do not
         * match then we know that this comparison should always fail.
         */
        int result = (input.length() ^ password.length());
        /*
         * Cycle through all of the characters up to the limit value
         * 
         * Important to note that this loop may return a false positive comparison if
         * the target string is a repeating set of characters in direct multiples of the
         * input string. This design fallacy is corrected by the original length
         * comparison above. The use of modulo this way is intended to prevent compiler
         * and memory optimizations for retrieving the same char position in sequence.
         */
        for (int i = 0; i < loops; i++) {
            result |= (input.charAt(i % input.length()) ^ password.charAt(i % password.length()));
        }

        return (result == 0);
    }
}
