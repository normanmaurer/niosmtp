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

import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.SMTPClientTransportFactory;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;

/**
 * {@link SMTPClientTransportFactory} which uses NETTY for the transport implementation
 * 
 * @author Norman Maurer
 *
 */
public class NettySMTPClientTransportFactory implements SMTPClientTransportFactory{
    // niosmtp uses slf4j so also configure it for netty
    static {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }
    private final ClientSocketChannelFactory factory;
    private final SMTPClientSessionFactory sessionFactory;

    private final static SMTPClientSessionFactory FACTORY = new SMTPClientSessionFactory() {
        
        @Override
        public SMTPClientSession newSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine) {
            return new NettySMTPClientSession(channel, logger, config, mode, engine);
        }
    };

    public NettySMTPClientTransportFactory(final ClientSocketChannelFactory factory, SMTPClientSessionFactory sessionFactory) {
        this.factory = factory;
        this.sessionFactory = sessionFactory;
    }

    /**
     * Create a new NIO based {@link SMTPClientTransportFactory}
     * 
     * @param workerCount
     * @return factory
     */
    public static SMTPClientTransportFactory createNio(int workerCount) {
        return new NettySMTPClientTransportFactory(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), workerCount), FACTORY);
    }
    
    /**
     * Create a new NIO based {@link SMTPClientTransportFactory}
     * 
     * @return factory
     */
    public static SMTPClientTransportFactory createNio() {
        return new NettySMTPClientTransportFactory(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()), FACTORY);
    }
    
    /**
     * Create a new OIO based {@link SMTPClientTransportFactory}
     * 
     * @return factory
     */
    public static SMTPClientTransportFactory createOio() {
        return new NettySMTPClientTransportFactory(new OioClientSocketChannelFactory(Executors.newCachedThreadPool()), FACTORY);
    }
    
    @Override
    public SMTPClientTransport createPlain() {
        return new NettySMTPClientTransport(SMTPDeliveryMode.PLAIN, null, factory, sessionFactory);
    }
    

    @Override
    public SMTPClientTransport createSMTPS(SSLContext context) {
        return new NettySMTPClientTransport(SMTPDeliveryMode.SMTPS, context, factory, sessionFactory);
    }
    

    @Override
    public SMTPClientTransport createStartTLS(SSLContext context, boolean failOnNoSupport) {
        SMTPDeliveryMode mode;
        if (failOnNoSupport) {
            mode = SMTPDeliveryMode.STARTTLS_DEPEND;
        } else {
            mode = SMTPDeliveryMode.STARTTLS_TRY;
        }
        return new NettySMTPClientTransport(mode, context, factory, sessionFactory);
    }
    
}
