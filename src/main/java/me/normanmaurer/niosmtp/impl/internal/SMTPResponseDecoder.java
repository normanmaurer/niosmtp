/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* Selene licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package me.normanmaurer.niosmtp.impl.internal;

import java.nio.charset.Charset;

import me.normanmaurer.niosmtp.SMTPResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OneToOneDecoder} which decodes {@link SMTPResponse}'s. It also handles
 * multi-line responses.
 * 
 * 
 * @author Norman Maurer
 * 
 */
class SMTPResponseDecoder extends OneToOneDecoder {
    private final static Charset CHARSET = Charset.forName("US-ASCII");
    private final Logger logger = LoggerFactory.getLogger(SMTPResponseDecoder.class);
    
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof ChannelBuffer) {
            ChannelBuffer line = (ChannelBuffer) msg;

            SMTPResponseImpl response = (SMTPResponseImpl) ctx.getAttachment();

            // The separator must be on index 3 as the return code has always 3
            // digits
            int separator = line.getByte(3);

            if (separator == ' ') {
                // Ok we had a ' ' separator which means this was the end of the
                // SMTPResponse
                if (response == null) {
                    int code = Integer.parseInt(line.readBytes(3).toString(CHARSET));
                    response = new SMTPResponseImpl(code);
                }
                if (line.readable()) {
                    response.addLine(line.toString(CHARSET));

                }
                ctx.setAttachment(null);
                if (logger.isDebugEnabled()) {
                    logger.debug("Channel " + ctx.getChannel().getId() + " received: [" + response.toString() + "]");
                }
                return response;
            } else if (separator == '-') {
                // The '-' separator is used for multi-line responses so just
                // add it to the response
                if (response == null) {
                    int code = Integer.parseInt(line.readBytes(3).toString(CHARSET));
                    response = new SMTPResponseImpl(code);
                    ctx.setAttachment(response);

                }
                if (line.readable()) {
                    response.addLine(line.toString(CHARSET));
                }
            }

            return null;
        } else {
            return msg;
        }

    }

}
