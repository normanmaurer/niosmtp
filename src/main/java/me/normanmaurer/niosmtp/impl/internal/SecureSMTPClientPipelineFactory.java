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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPClientTransport.DeliveryMode;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
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
public class SecureSMTPClientPipelineFactory extends SMTPClientPipelineFactory{

    private final static SslHandshakeHandler SSL_HANDSHAKE_HANDLER = new SslHandshakeHandler();
    
    private final SSLContext context;
    private final DeliveryMode mode;

    public SecureSMTPClientPipelineFactory(SMTPResponseCallback callback, Timer timer, int readTimeout, SSLContext context, DeliveryMode mode) {
        super(callback, timer, readTimeout);
        this.context = context;
        this.mode = mode;
    }


    @Override
    public ChannelPipeline getPipeline() throws Exception {        
        ChannelPipeline cp = super.getPipeline();

        if (mode == DeliveryMode.SMTPS) {
            cp.addFirst("sslHandshakeHandler", SSL_HANDSHAKE_HANDLER);

            final SslHandler sslHandler = new SslHandler(createSSLClientEngine(), false);
            cp.addFirst("sslHandler", sslHandler);
        }
        return cp;
    }
    


    private SSLEngine createSSLClientEngine() {
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);
        return engine;
    }
    

    @Override
    protected ConnectHandler createConnectHandler() {
        if (mode == DeliveryMode.SMTPS || mode == DeliveryMode.PLAIN) {
            return super.createConnectHandler();
        } else {
            return new ConnectHandler(true, createSSLClientEngine());
        }
    }


    /**
     * {@link SimpleChannelUpstreamHandler} which takes care to call {@link SslHandler#handshake()} after the channel is connected
     * 
     * @author Norman Maurer
     *
     */
    private final static class SslHandshakeHandler extends SimpleChannelUpstreamHandler {

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
            sslHandler.handshake();
        }
        
    }

}
