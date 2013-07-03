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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.impl.FutureResultImpl;
import me.normanmaurer.niosmtp.transport.netty.SMTPClientSessionFactory;

import org.slf4j.Logger;

/** * 
 * The special thing about this implementation is that I will remove itself from the {@link ChannelPipeline} after the first {@link #messageReceived(ChannelHandlerContext, SMTPResponse)}
 * was executed. It also takes care to create the {@link SMTPClientSession} and inject it the wrapped {@link SMTPClientFutureListener}.
 * 
 * @author Norman Maurer
 *
 */
public class SMTPConnectHandler extends SimpleChannelInboundHandler<SMTPResponse> {

    private final SSLEngine engine;
    private final SMTPDeliveryMode mode;
    private final SMTPClientFutureImpl<FutureResult<SMTPResponse>> future;
    private final Logger logger;
    private final SMTPClientConfig config;
    private final SMTPClientSessionFactory factory;
    private final static AttributeKey<SMTPClientSession> SESSION_ATTR = new AttributeKey<SMTPClientSession>("session");
    
    public SMTPConnectHandler(SMTPClientFutureImpl<FutureResult<SMTPResponse>> future, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine, SMTPClientSessionFactory factory){
        this.future = future;
        this.engine = engine;
        this.mode = mode;
        this.logger = logger;
        this.config = config;
        this.factory = factory;
    }
    
    private SMTPClientSession getSession(ChannelHandlerContext ctx) {
        SMTPClientSession attachment = ctx.attr(SESSION_ATTR).get();
        
        if (attachment == null) {
            attachment =  factory.newSession(ctx.channel(), logger, config, mode, engine);
            ctx.attr(SESSION_ATTR).set(attachment);
        }
        return attachment;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().pipeline().remove(this);
        future.setSMTPClientSession(getSession(ctx));
        future.setResult(FutureResult.create(cause));
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, SMTPResponse msg) throws Exception {
        ctx.channel().pipeline().remove(this);
        future.setSMTPClientSession(getSession(ctx));
        future.setResult(new FutureResultImpl<SMTPResponse>(msg));        
    }
}
