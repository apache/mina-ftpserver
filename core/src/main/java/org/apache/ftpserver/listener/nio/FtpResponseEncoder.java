/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.ftpserver.listener.nio;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * A {@link MessageEncoder} that encodes {@link FtpReply}.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class FtpResponseEncoder extends ProtocolEncoderAdapter {
    private static final ThreadLocal<CharsetEncoder> ENCODER = new ThreadLocal<CharsetEncoder>() {
        @Override
        protected CharsetEncoder initialValue() {
            return StandardCharsets.UTF_8.newEncoder();
        }
    };

    public void encode(IoSession session, Object message,
            ProtocolEncoderOutput out) throws Exception {
        String value = message.toString();

        IoBuffer buf = IoBuffer.allocate(value.length()).setAutoExpand(true);

        buf.putString(value, ENCODER.get());

        buf.flip();
        out.write(buf);
    }
}
