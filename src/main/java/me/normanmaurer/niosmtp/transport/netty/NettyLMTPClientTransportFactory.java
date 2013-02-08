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

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.logging.InternalLoggerFactory;
import io.netty.logging.Slf4JLoggerFactory;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransportFactory;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

import org.slf4j.Logger;

/**
 * 
 * @author Norman Maurer
 *
 */
public class NettyLMTPClientTransportFactory extends NettySMTPClientTransportFactory {
    // niosmtp uses slf4j so also configure it for netty
    static {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }
    private final static SMTPClientSessionFactory FACTORY = new SMTPClientSessionFactory() {
        
        @Override
        public SMTPClientSession newSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine) {
            return new NettyLMTPClientSession(channel, logger, config, mode, engine);
        }
    };
    
    public NettyLMTPClientTransportFactory(Class<? extends Channel> channel, EventLoopGroup group,  SMTPClientSessionFactory sessionFactory) {
        super(channel, group, sessionFactory);
    }

    /**
     * Create a new NIO based {@link SMTPClientTransportFactory}
     * 
     * @return factory
     */
    public static SMTPClientTransportFactory createNio() {
        return new NettySMTPClientTransportFactory(NioSocketChannel.class, new NioEventLoopGroup(), FACTORY);
    }
    
    /**
     * Create a new OIO based {@link SMTPClientTransportFactory}
     * 
     * @return factory
     */
    public static SMTPClientTransportFactory createOio() {
        return new NettySMTPClientTransportFactory(OioSocketChannel.class, new OioEventLoopGroup(), FACTORY);
    }
    
}
