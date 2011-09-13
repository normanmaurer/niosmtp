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

import me.normanmaurer.niosmtp.SMTPPipelinedRequest;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OneToOneEncoder} which takes care to encode {@link SMTPPipelinedRequest} messages
 * 
 * 
 * @author Norman Maurer
 *
 */
public class SMTPPipelinedRequestEncoder extends OneToOneEncoder implements SMTPClientConstants{
    private final Logger logger = LoggerFactory.getLogger(SMTPPipelinedRequestEncoder.class);

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        
        if (msg instanceof SMTPPipelinedRequest) {

            String request = StringUtils.toString((SMTPPipelinedRequest) msg);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Channel " + ctx.getChannel().getId() + " sent: [" + request + "]");
            }
            return ChannelBuffers.wrappedBuffer(request.getBytes(CHARSET));
        }
        return msg;
    }

}
