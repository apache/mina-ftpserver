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

package org.apache.ftpserver.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ftpserver.command.impl.ABOR;
import org.apache.ftpserver.command.impl.ACCT;
import org.apache.ftpserver.command.impl.APPE;
import org.apache.ftpserver.command.impl.AUTH;
import org.apache.ftpserver.command.impl.CDUP;
import org.apache.ftpserver.command.impl.CWD;
import org.apache.ftpserver.command.impl.DELE;
import org.apache.ftpserver.command.impl.DefaultCommandFactory;
import org.apache.ftpserver.command.impl.EPRT;
import org.apache.ftpserver.command.impl.EPSV;
import org.apache.ftpserver.command.impl.FEAT;
import org.apache.ftpserver.command.impl.HELP;
import org.apache.ftpserver.command.impl.LANG;
import org.apache.ftpserver.command.impl.LIST;
import org.apache.ftpserver.command.impl.MD5;
import org.apache.ftpserver.command.impl.MDTM;
import org.apache.ftpserver.command.impl.MFMT;
import org.apache.ftpserver.command.impl.MKD;
import org.apache.ftpserver.command.impl.MLSD;
import org.apache.ftpserver.command.impl.MLST;
import org.apache.ftpserver.command.impl.MODE;
import org.apache.ftpserver.command.impl.NLST;
import org.apache.ftpserver.command.impl.NOOP;
import org.apache.ftpserver.command.impl.OPTS;
import org.apache.ftpserver.command.impl.PASS;
import org.apache.ftpserver.command.impl.PASV;
import org.apache.ftpserver.command.impl.PBSZ;
import org.apache.ftpserver.command.impl.PORT;
import org.apache.ftpserver.command.impl.PROT;
import org.apache.ftpserver.command.impl.PWD;
import org.apache.ftpserver.command.impl.QUIT;
import org.apache.ftpserver.command.impl.REIN;
import org.apache.ftpserver.command.impl.REST;
import org.apache.ftpserver.command.impl.RETR;
import org.apache.ftpserver.command.impl.RMD;
import org.apache.ftpserver.command.impl.RNFR;
import org.apache.ftpserver.command.impl.RNTO;
import org.apache.ftpserver.command.impl.SITE;
import org.apache.ftpserver.command.impl.SITE_DESCUSER;
import org.apache.ftpserver.command.impl.SITE_HELP;
import org.apache.ftpserver.command.impl.SITE_STAT;
import org.apache.ftpserver.command.impl.SITE_WHO;
import org.apache.ftpserver.command.impl.SITE_ZONE;
import org.apache.ftpserver.command.impl.SIZE;
import org.apache.ftpserver.command.impl.STAT;
import org.apache.ftpserver.command.impl.STOR;
import org.apache.ftpserver.command.impl.STOU;
import org.apache.ftpserver.command.impl.STRU;
import org.apache.ftpserver.command.impl.SYST;
import org.apache.ftpserver.command.impl.TYPE;
import org.apache.ftpserver.command.impl.USER;

/**
 * Factory for {@link CommandFactory} instances
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class CommandFactoryFactory {

    private static final HashMap<String, Command> DEFAULT_COMMAND_MAP = new HashMap<String, Command>();

    static {
        // first populate the default command list
        DEFAULT_COMMAND_MAP.put("ABOR", new ABOR());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("ACCT", new ACCT());                    // rfc959, 4.1.1
        // ADAT, rfc2228, 3?
        // ALLO, rfc959?
        DEFAULT_COMMAND_MAP.put("APPE", new APPE());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("AUTH", new AUTH());                    // rfc2228, 3
        //AVBL, draft-peterson-streamlined-ftp-command-extensions?
        // CCC, rfc2228, 3?
        DEFAULT_COMMAND_MAP.put("CDUP", new CDUP());                    // rfc959, 4.1.1
        // CONF, rfc2228, 3?
        // CSID, draft-peterson-streamlined-ftp-command-extensions?
        DEFAULT_COMMAND_MAP.put("CWD", new CWD());                      // rfc959, 4.1.1
        DEFAULT_COMMAND_MAP.put("DELE", new DELE());                    // rfc959, 4.1.3
        // DSIZ, draft-peterson-streamlined-ftp-command-extensions?
        // ENC, rfc2228, 3?
        DEFAULT_COMMAND_MAP.put("EPRT", new EPRT());                    // rfc2428, 2
        DEFAULT_COMMAND_MAP.put("EPSV", new EPSV());                    // rfc2428, 3
        DEFAULT_COMMAND_MAP.put("FEAT", new FEAT());                    // rfc2389, 3
        DEFAULT_COMMAND_MAP.put("HELP", new HELP());                    // rfc959, 4.1.3
        // HOST, rfc7151?
        DEFAULT_COMMAND_MAP.put("LANG", new LANG());                    // rfc2640, 4.1
        DEFAULT_COMMAND_MAP.put("LIST", new LIST());                    // rfc959, 4.1.3
        // LPRT, rfc1639 2?
        // LPSV, rfc1639 2?
        DEFAULT_COMMAND_MAP.put(MD5.MD5, new MD5());                    // draft-twine-ftpmd5-00.txt, 3.1
        // MIC, rfc2228, 3?
        DEFAULT_COMMAND_MAP.put(MD5.MMD5, new MD5());                   // draft-twine-ftpmd5-00.txt , 3.2
        DEFAULT_COMMAND_MAP.put("MDTM", new MDTM());                    // rfc3659, 3
        // "MFCT, draft-somers-ftp-mfxx, 4
        // "MFF, draft-somers-ftp-mfxx, 5
        DEFAULT_COMMAND_MAP.put("MFMT", new MFMT());                    // draft-somers-ftp-mfxx, 3
        DEFAULT_COMMAND_MAP.put("MKD", new MKD());                      // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("MLSD", new MLSD());                    // rfc3659, 7
        DEFAULT_COMMAND_MAP.put("MLST", new MLST());                    // rfc3659, 7
        DEFAULT_COMMAND_MAP.put("MODE", new MODE());                    // rfc959, 4.1.2
        DEFAULT_COMMAND_MAP.put("NLST", new NLST());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("NOOP", new NOOP());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("OPTS", new OPTS());                    // rfc2389, 4
        DEFAULT_COMMAND_MAP.put("PASS", new PASS());                    // rfc959, 4.1.1
        DEFAULT_COMMAND_MAP.put("PASV", new PASV());                    // rfc959, 4.1.2
        DEFAULT_COMMAND_MAP.put("PBSZ", new PBSZ());                    // rfc2228, 3
        DEFAULT_COMMAND_MAP.put("PORT", new PORT());                    // rfc959, 4.1.2
        DEFAULT_COMMAND_MAP.put("PROT", new PROT());                    // rfc2228, 3
        DEFAULT_COMMAND_MAP.put("PWD", new PWD());                      // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("QUIT", new QUIT());                    // rfc959, 4.1.1
        DEFAULT_COMMAND_MAP.put("REIN", new REIN());                    // rfc959, 4.1.1
        DEFAULT_COMMAND_MAP.put("REST", new REST());                    // rfc959, 4.1.3, rfc3659, 5
        DEFAULT_COMMAND_MAP.put("RETR", new RETR());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("RMD", new RMD());                      // rfc959, 4.1.3
        // RMDA, draft-peterson-streamlined-ftp-command-extensions?
        DEFAULT_COMMAND_MAP.put("RNFR", new RNFR());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("RNTO", new RNTO());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("SITE", new SITE());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("SITE_DESCUSER", new SITE_DESCUSER());  // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("SITE_HELP", new SITE_HELP());          // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("SITE_STAT", new SITE_STAT());          // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("SITE_WHO", new SITE_WHO());            // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("SITE_ZONE", new SITE_ZONE());          // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("SIZE", new SIZE());                    // rfc3659, 4
        // SMNT, rfc959?
        // SPSV, draft-rosenau-ftp-single-port
        DEFAULT_COMMAND_MAP.put("STAT", new STAT());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("STOR", new STOR());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("STOU", new STOU());                    // rfc959, 4.1.3
        DEFAULT_COMMAND_MAP.put("STRU", new STRU());                    // rfc959, 4.1.2
        DEFAULT_COMMAND_MAP.put("SYST", new SYST());                    // rfc959, 4.1.3
        // THMB, draft-peterson-streamlined-ftp-command-extensions
        DEFAULT_COMMAND_MAP.put("TYPE", new TYPE());                    // rfc959, 4.1.2
        // TVFS, rfc3659, 6?
        DEFAULT_COMMAND_MAP.put("USER", new USER());                    // rfc959, 4.1.1
    }

    /** The commands map */
    private Map<String, Command> commandMap = new HashMap<String, Command>();

    /** A flag indicating of we have non-default commands */
    private boolean useDefaultCommands = true;

    /**
     * Create an {@link CommandFactory} based on the configuration on the factory.
     * @return The {@link CommandFactory}
     */
    public CommandFactory createCommandFactory() {
        
        Map<String, Command> mergedCommands = new HashMap<String, Command>();
        
        if (useDefaultCommands) {
            mergedCommands.putAll(DEFAULT_COMMAND_MAP);
        }
        
        mergedCommands.putAll(commandMap);
        
        return new DefaultCommandFactory(mergedCommands);
    }
    
    /**
     * Are default commands used?
     * 
     * @return true if default commands are used
     */
    public boolean isUseDefaultCommands() {
        return useDefaultCommands;
    }

    /**
     * Sets whether the default commands will be used.
     * 
     * @param useDefaultCommands <code>true</code> if default commands should be used
     */
    public void setUseDefaultCommands(final boolean useDefaultCommands) {
        this.useDefaultCommands = useDefaultCommands;
    }

    /**
     * Get the installed commands
     * 
     * @return The installed commands
     */
    public Map<String, Command> getCommandMap() {
        return commandMap;
    }

    /**
     * Add or override a command.
     * @param commandName The command name, e.g. STOR
     * @param command The command
     */
    public void addCommand(String commandName, Command command) {
        if (commandName == null) {
            throw new NullPointerException("commandName can not be null");
        }
        
        if (command == null) {
            throw new NullPointerException("command can not be null");
        }
        
        commandMap.put(commandName.toUpperCase(), command);
    }
    
    /**
     * Set commands to add or override to the default commands
     * 
     * @param commandMap The map of commands, the key will be used to map to requests.
     */
    public void setCommandMap(final Map<String, Command> commandMap) {
        if (commandMap == null) {
            throw new NullPointerException("commandMap can not be null");
        }

        this.commandMap.clear();

        for (Entry<String, Command> entry : commandMap.entrySet()) {
            this.commandMap.put(entry.getKey().toUpperCase(), entry.getValue());
        }
    }
}
