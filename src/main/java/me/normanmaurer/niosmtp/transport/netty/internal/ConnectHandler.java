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

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;

/**
 * {@link SimpleChannelUpstreamHandler} sub-class which acts as adapter for an {@link SMTPResponseCallback}. 
 * 
 * The special thing about this implementation is that I will remove itself from the {@link ChannelPipeline} after the first {@link #messageReceived(ChannelHandlerContext, MessageEvent)}
 * was executed. It also takes care to create the {@link NettySMTPClientSession} and inject it the wrapped {@link SMTPResponseCallback}.
 * 
 * @author Norman Maurer
 *
 */
class ConnectHandler extends SimpleChannelUpstreamHandler {

    private final SSLEngine engine;
    private final SMTPDeliveryMode mode;
    private final SMTPResponseCallback callback;
    private final Logger logger;
    private final SMTPClientConfig config;

    public ConnectHandler(SMTPResponseCallback callback, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine){
        this.callback = callback;
        this.engine = engine;
        this.mode = mode;
        this.logger = logger;
        this.config = config;
    }
    
    private SMTPClientSession createSession(ChannelHandlerContext ctx) {
        Object attachment = ctx.getAttachment();
        if (attachment == null) {
            attachment =  NettySMTPClientSession.create(ctx.getChannel(), logger, config, mode, engine);
            ctx.setAttachment(attachment);
        }
        return (SMTPClientSession) attachment;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof SMTPResponse) {
            callback.onResponse(createSession(ctx), (SMTPResponse) msg);
            ctx.getChannel().getPipeline().remove(this);
        } else {
            super.messageReceived(ctx, e);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        callback.onException(createSession(ctx), e.getCause());
        ctx.getChannel().getPipeline().remove(this);
    }
}
