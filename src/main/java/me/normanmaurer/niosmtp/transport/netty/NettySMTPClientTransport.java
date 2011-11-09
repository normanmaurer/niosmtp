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

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.delivery.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.netty.internal.SMTPClientPipelineFactory;
import me.normanmaurer.niosmtp.transport.netty.internal.SecureSMTPClientPipelineFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

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
    private final Timer timer = new HashedWheelTimer();
    private final ClientSocketChannelFactory factory;
    private final DefaultChannelGroup channelGroup = new DefaultChannelGroup();
    private final SMTPClientSessionFactory sessionFactory;

    NettySMTPClientTransport(SMTPDeliveryMode mode, SSLContext context, ClientSocketChannelFactory factory, SMTPClientSessionFactory sessionFactory) {
        this.context = context;
        this.mode = mode;
        this.factory = factory;
        this.sessionFactory = sessionFactory;
    }

    
    
    
    
    @Override
    public SMTPClientFuture<FutureResult<SMTPResponse>> connect(InetSocketAddress remote, SMTPClientConfig config) {
        SMTPClientFutureImpl<FutureResult<SMTPResponse>> future = new SMTPClientFutureImpl<FutureResult<SMTPResponse>>();
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setOption("connectTimeoutMillis", config.getConnectionTimeout() * 1000);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("reuseAddress", true);

        ChannelPipelineFactory cp;
        switch (mode) {
        case PLAIN:
            cp = new SMTPClientPipelineFactory(future, config, timer, sessionFactory);
            break;
        case SMTPS:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_TRY:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_DEPEND:
            cp = new SecureSMTPClientPipelineFactory(future, config, timer,context, mode, sessionFactory);
            break;
        default:
            throw new IllegalArgumentException("Unknown DeliveryMode " + mode);
        }

        bootstrap.setPipelineFactory(cp);
        InetSocketAddress local = config.getLocalAddress();
        bootstrap.connect(remote, local).addListener(new ChannelFutureListener() {
            
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channelGroup.add(future.getChannel());
                }
            }
        });
        return future;
    }
    

    @Override
    public SMTPDeliveryMode getDeliveryMode() {
        return mode;
    }
    
    @Override
    public void destroy() {
        timer.stop();
        channelGroup.close().awaitUninterruptibly();
        factory.releaseExternalResources();
    }

}
