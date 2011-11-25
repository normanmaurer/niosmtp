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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.netty.NettyConstants;
import me.normanmaurer.niosmtp.transport.netty.SMTPClientSessionFactory;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.Timer;

/**
 * {@link ChannelPipelineFactory} which is used for SMTPS connections
 * 
 * @author Norman Maurer
 * 
 *
 */
public class SecureSMTPClientPipelineFactory extends SMTPClientPipelineFactory implements NettyConstants{

    private final static SslHandshakeHandler SSL_HANDSHAKE_HANDLER = new SslHandshakeHandler();
    private final SSLContext context;
    private final SMTPDeliveryMode mode;

    public SecureSMTPClientPipelineFactory(SMTPClientFutureImpl<FutureResult<SMTPResponse>> future, SMTPClientConfig config, Timer timer, SSLContext context, SMTPDeliveryMode mode, SMTPClientSessionFactory factory) {
        super(future, config, timer, factory);
        this.context = context;
        this.mode = mode;
    }


    @Override
    public ChannelPipeline getPipeline() throws Exception {        
        ChannelPipeline cp = super.getPipeline();

        if (mode == SMTPDeliveryMode.SMTPS) {
            cp.addFirst(SSL_HANDSHAKE_HANDLER_KEY, SSL_HANDSHAKE_HANDLER);

            final SslHandler sslHandler = new SslHandler(createSSLClientEngine(), false);
            cp.addFirst(SSL_HANDLER_KEY, sslHandler);
        }
        return cp;
    }
    


    private SSLEngine createSSLClientEngine() {
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);
        return engine;
    }
    

    @Override
    protected SMTPConnectHandler createConnectHandler() {
        if (mode == SMTPDeliveryMode.SMTPS || mode == SMTPDeliveryMode.PLAIN) {
            return super.createConnectHandler();
        } else {
            return new SMTPConnectHandler(future, LOGGER, config, mode, createSSLClientEngine(), factory);
        }
    }


    /**
     * {@link SimpleChannelUpstreamHandler} which takes care to call {@link SslHandler#handshake()} after the channel is connected
     * 
     * @author Norman Maurer
     *
     */
    protected final static class SslHandshakeHandler extends SimpleChannelUpstreamHandler {
        private static final ChannelFutureListener HANDSHAKE_LISTENER = new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    Channels.fireExceptionCaught(future.getChannel(), future.getCause());
                }
            }
            
        };
        
        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
            sslHandler.handshake().addListener(HANDSHAKE_LISTENER);
        }
        
    }

}
