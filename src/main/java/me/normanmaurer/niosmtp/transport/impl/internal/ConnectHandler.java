package me.normanmaurer.niosmtp.transport.impl.internal;

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
import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;

public class ConnectHandler extends SimpleChannelUpstreamHandler {

    private SSLEngine engine;
    private boolean startTLS;
    private SMTPResponseCallback callback;
    private Logger logger;

    public ConnectHandler(SMTPResponseCallback callback, Logger logger, boolean startTLS, SSLEngine engine){
        this.callback = callback;
        this.engine = engine;
        this.startTLS = startTLS;
        this.logger = logger;
    }
    
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof SMTPResponse) {
            callback.onResponse(new NettySMTPClientSession(ctx.getChannel(), logger, startTLS, engine), (SMTPResponse) msg);
            ctx.getChannel().getPipeline().remove(this);
        } else {
            super.messageReceived(ctx, e);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        callback.onException(new NettySMTPClientSession(ctx.getChannel(), logger, startTLS, engine), e.getCause());
        ctx.getChannel().getPipeline().remove(this);

    }
}
