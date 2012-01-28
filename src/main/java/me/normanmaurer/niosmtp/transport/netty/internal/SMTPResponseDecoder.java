/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* niosmtp licenses this file to You under the Apache License, Version 2.0
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
package me.normanmaurer.niosmtp.transport.netty.internal;

import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPResponseImpl;
import me.normanmaurer.niosmtp.core.StringUtils;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;

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
public class SMTPResponseDecoder extends OneToOneDecoder implements SMTPClientConstants{
    private final static Logger logger = LoggerFactory.getLogger(SMTPResponseDecoder.class);

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof ChannelBuffer) {
            ChannelBuffer line = (ChannelBuffer) msg;

            SMTPResponseImpl response = (SMTPResponseImpl) ctx.getAttachment();

            // The separator must be on index 3 as the return code has always 3
            // digits
            int separator = line.getByte(3);

            if (separator == SMTPResponse.LAST_SEPERATOR) {
                // Ok we had a ' ' separator which means this was the end of the
                // SMTPResponse
                if (response == null) {
                    int code = Integer.parseInt(line.readBytes(3).toString(CHARSET));
                    response = new SMTPResponseImpl(code);
                    // skip the next space
                    line.skipBytes(1);
                } else {
                    // skip the code and the next space
                    line.skipBytes(4);
                }
                
                if (line.readable()) {
                    response.addLine(line.toString(CHARSET));

                }
                ctx.setAttachment(null);
                if (logger.isDebugEnabled()) {
                    logger.debug("Channel " + ctx.getChannel().getId() + " received: [" + StringUtils.toString(response) + "]");
                }
                return response;
            } else if (separator == SMTPResponse.SEPERATOR) {
                // The '-' separator is used for multi-line responses so just
                // add it to the response
                if (response == null) {
                    int code = Integer.parseInt(line.readBytes(3).toString(CHARSET));
                    response = new SMTPResponseImpl(code);
                    ctx.setAttachment(response);
                    
                    // skip the next space
                    line.skipBytes(1);

                } else {
                    // skip the code and the next space
                    line.skipBytes(4);
                }
                if (line.readable()) {
                    response.addLine(line.toString(CHARSET));
                }
            } else {
                // throw exception if the response does not have a valid format
                throw new SMTPException("Unable to parse SMTPResponse: " + line.toString(CHARSET));
            }

            return null;
        } else {
            return msg;
        }

    }

}
