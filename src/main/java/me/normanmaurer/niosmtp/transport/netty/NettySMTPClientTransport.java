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
package me.normanmaurer.niosmtp.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.logging.InternalLoggerFactory;
import io.netty.logging.Slf4JLoggerFactory;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.netty.internal.SMTPClientPipelineInitializer;
import me.normanmaurer.niosmtp.transport.netty.internal.SecureSMTPClientPipelineInitializer;


/**
 * {@link SMTPClientTransport} which uses Netty under the hood
 * 
 * @author Norman Maurer
 *
 */
class NettySMTPClientTransport implements SMTPClientTransport{
    // niosmtp uses slf4j so also configure it for netty
    static {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }
    private final SSLContext context;
    private final SMTPDeliveryMode mode;
    private final Class<? extends Channel> channel;
    private final DefaultChannelGroup channelGroup = new DefaultChannelGroup();
    private final SMTPClientSessionFactory sessionFactory;
    private EventLoopGroup group;

    NettySMTPClientTransport(SMTPDeliveryMode mode, SSLContext context, Class<? extends Channel> channel, EventLoopGroup group, SMTPClientSessionFactory sessionFactory) {
        this.context = context;
        this.mode = mode;
        this.channel = channel;
        this.sessionFactory = sessionFactory;
        this.group = group;
    }

    @Override
    public SMTPClientFuture<FutureResult<SMTPResponse>> connect(InetSocketAddress remote, SMTPClientConfig config) {
        SMTPClientFutureImpl<FutureResult<SMTPResponse>> future = new SMTPClientFutureImpl<FutureResult<SMTPResponse>>();
        Bootstrap bootstrap = new Bootstrap().channel(channel).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeout() * 1000);
        bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.group(group);
        ChannelInitializer<SocketChannel> cp;
        switch (mode) {
        case PLAIN:
            cp = new SMTPClientPipelineInitializer(future, config, sessionFactory);
            break;
        case SMTPS:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_TRY:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_DEPEND:
            cp = new SecureSMTPClientPipelineInitializer(future, config,context, mode, sessionFactory);
            break;
        default:
            throw new IllegalArgumentException("Unknown DeliveryMode " + mode);
        }

        bootstrap.handler(cp);
        InetSocketAddress local = config.getLocalAddress();
        bootstrap.localAddress(local).remoteAddress(remote).connect().addListener(new ChannelFutureListener() {
            
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channelGroup.add(future.channel());
                }
            }
        }).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        return future;
    }

    @Override
    public SMTPDeliveryMode getDeliveryMode() {
        return mode;
    }
    
    @Override
    public void destroy() {
        channelGroup.close().awaitUninterruptibly();
        group.shutdown();
    }

}
