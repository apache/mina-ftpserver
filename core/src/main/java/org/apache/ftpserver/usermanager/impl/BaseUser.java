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

package org.apache.ftpserver.usermanager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Generic user class. The user attributes are:
 * <ul>
 * <li>userid</li>
 * <li>userpassword</li>
 * <li>enableflag</li>
 * <li>homedirectory</li>
 * <li>writepermission</li>
 * <li>idletime</li>
 * <li>uploadrate</li>
 * <li>downloadrate</li>
 * </ul>
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */

public class BaseUser implements User {
    private String name = null;

    private String password = null;

    private int maxIdleTimeSec = 0; // no limit

    private String homeDir = null;

    private boolean isEnabled = true;

    private List<? extends Authority> authorities = new ArrayList<>();

    /**
     * Default constructor.
     */
    public BaseUser() {
    }

    /**
     * Copy constructor.
     *
     * @param user The user to copy
     */
    public BaseUser(User user) {
        name = user.getName();
        password = user.getPassword();
        authorities = user.getAuthorities();
        maxIdleTimeSec = user.getMaxIdleTime();
        homeDir = user.getHomeDirectory();
        isEnabled = user.getEnabled();
    }

    /**
     * Get the user name.
     *
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set user name.
     *
     * @param name The user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the user password.
     *
     * @return The user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set user password.
     *
     * @param pass The user's password
     */
    public void setPassword(String pass) {
        password = pass;
    }

    /**
     * Get the user's authorities
     *
     * @return the user's authorities
     */
    public List<Authority> getAuthorities() {
        if (authorities != null) {
            return Collections.unmodifiableList(authorities);
        } else {
            return null;
        }
    }

    /**
     * Set the list of authorities
     *
     * @param authorities The list of authorities to set
     */
    public void setAuthorities(List<Authority> authorities) {
        if (authorities != null) {
            this.authorities = Collections.unmodifiableList(authorities);
        } else {
            this.authorities = null;
        }
    }

    /**
     * Get the maximum idle time in second.
     *
     * @return the maximum idle time for this user
     */
    public int getMaxIdleTime() {
        return maxIdleTimeSec;
    }

    /**
     * Set the maximum idle time in second.
     *
     * @param idleSec The maximum idle time in seconds
     */
    public void setMaxIdleTime(int idleSec) {
        maxIdleTimeSec = idleSec;
        if (maxIdleTimeSec < 0) {
            maxIdleTimeSec = 0;
        }
    }

    /**
     * Get the user enable status.
     *
     * @return Tell if the user is enabled
     */
    public boolean getEnabled() {
        return isEnabled;
    }

    /**
     * Set the user enable status.
     *
     * @param enb Enable or disable the user
     */
    public void setEnabled(boolean enb) {
        isEnabled = enb;
    }

    /**
     * Get the user home directory.
     *
     * @return The user's home directory
     */
    public String getHomeDirectory() {
        return homeDir;
    }

    /**
     * Set the user home directory.
     *
     * @param home The user's home
     */
    public void setHomeDirectory(String home) {
        homeDir = home;
    }

    /**
     * String representation.
     *
     * @return The user as a String
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public AuthorizationRequest authorize(AuthorizationRequest request) {
        // check for no authorities at all
        if (authorities == null) {
            return null;
        }

        boolean someoneCouldAuthorize = false;

        for (Authority authority : authorities) {
            if (authority.canAuthorize(request)) {
                someoneCouldAuthorize = true;

                request = authority.authorize(request);

                // authorization failed, return null
                if (request == null) {
                    return null;
                }
            }
        }

        if (someoneCouldAuthorize) {
            return request;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
        List<Authority> selected = new ArrayList<>();

        for (Authority authority : authorities) {
            if (authority.getClass().equals(clazz)) {
                selected.add(authority);
            }
        }

        return selected;
    }
}
