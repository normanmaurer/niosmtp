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

import me.normanmaurer.niosmtp.SMTPRequest;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OneToOneEncoder} which encoded {@link SMTPRequest} objects to {@link ChannelBuffer}
 * 
 * @author Norman Maurer
 *
 */
class SMTPRequestEncoder extends OneToOneEncoder implements SMTPClientConstants{
    private final Logger logger = LoggerFactory.getLogger(SMTPRequestEncoder.class);

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel arg1, Object msg) throws Exception {
        if (msg instanceof SMTPRequest) {
            SMTPRequest req = (SMTPRequest) msg;
            String request = StringUtils.toString((SMTPRequest) req);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Channel " + ctx.getChannel().getId() + " sent: [" + request + "]");
            }
            
          
            return ChannelBuffers.wrappedBuffer(request.getBytes(CHARSET));
        }
        return msg;
    }

}
