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

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.netty.SMTPClientSessionFactory;

/**
 * {@link SMTPClientPipelineInitializer} which is used for SMTPS connections
 * 
 * @author Norman Maurer
 * 
 *
 */
public class SecureSMTPClientPipelineInitializer extends SMTPClientPipelineInitializer {

    private final SSLContext context;
    private final SMTPDeliveryMode mode;

    public SecureSMTPClientPipelineInitializer(SMTPClientFutureImpl<FutureResult<SMTPResponse>> future, SMTPClientConfig config, SSLContext context, SMTPDeliveryMode mode, SMTPClientSessionFactory factory) {
        super(future, config, factory);
        this.context = context;
        this.mode = mode;
    }


    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        super.initChannel(channel);
        ChannelPipeline cp = channel.pipeline();

        if (mode == SMTPDeliveryMode.SMTPS) {
            final SslHandler sslHandler = new SslHandler(createSSLClientEngine(), false);
            cp.addFirst(SSL_HANDLER_KEY, sslHandler);
        }
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

}
