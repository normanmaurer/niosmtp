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

import java.io.InputStream;
import java.util.LinkedList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPClient.DeliveryMode;
import me.normanmaurer.niosmtp.SMTPClientConfig;

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

    public SecureSMTPClientPipelineFactory(SMTPClientFutureImpl future, String mailFrom, LinkedList<String> recipients, InputStream msg, SMTPClientConfig config, Timer timer, DeliveryMode mode, SSLContext context) {
        super(future, mailFrom, recipients, msg, config, timer);
        this.context = context;
        this.mode = mode;
    }


    @Override
    public ChannelPipeline getPipeline() throws Exception {        
        ChannelPipeline cp = super.getPipeline();

        if (mode == DeliveryMode.SMTPS) {
            cp.addFirst("sslHandshakeHandler", SSL_HANDSHAKE_HANDLER);
            SSLEngine engine = context.createSSLEngine();
            engine.setUseClientMode(true);
            final SslHandler sslHandler = new SslHandler(engine, false);
            cp.addFirst("sslHandler", sslHandler);
        }
        return cp;
    }
    
    @Override
    protected SMTPClientHandler createSMTPClientHandler(SMTPClientFutureImpl future, String mailFrom, LinkedList<String> recipients, InputStream msg, SMTPClientConfig config) {
        if (mode == DeliveryMode.SMTPS) {
            return super.createSMTPClientHandler(future, mailFrom, recipients, msg, config);
        } else {
            boolean dependOnStartTLS;
            if (mode == DeliveryMode.STARTTLS_DEPEND) {
                dependOnStartTLS = true;
            } else {
                dependOnStartTLS = false;
            }
            return new SMTPClientHandler(future, mailFrom, recipients, msg, config, dependOnStartTLS, context.createSSLEngine());
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
