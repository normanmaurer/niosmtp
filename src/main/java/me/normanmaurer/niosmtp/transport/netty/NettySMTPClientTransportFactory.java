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

import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.SMTPClientTransportFactory;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;

/**
 * {@link SMTPClientTransportFactory} which uses NETTY for the transport implementation
 * 
 * @author Norman Maurer
 *
 */
public class NettySMTPClientTransportFactory implements SMTPClientTransportFactory{

    private final ClientSocketChannelFactory factory;


    public NettySMTPClientTransportFactory(final ClientSocketChannelFactory factory) {
        this.factory = factory;
    }

    /**
     * Create a new NIO based {@link SMTPClientTransportFactory}
     * 
     * @param workerCount
     * @return factory
     */
    public static SMTPClientTransportFactory createNio(int workerCount) {
        return new NettySMTPClientTransportFactory(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), workerCount));
    }
    
    /**
     * Create a new NIO based {@link SMTPClientTransportFactory}
     * 
     * @return factory
     */
    public static SMTPClientTransportFactory createNio() {
        return new NettySMTPClientTransportFactory(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
    }
    
    /**
     * Create a new OIO based {@link SMTPClientTransportFactory}
     * 
     * @return factory
     */
    public static SMTPClientTransportFactory createOio() {
        return new NettySMTPClientTransportFactory(new OioClientSocketChannelFactory(Executors.newCachedThreadPool()));
    }
    
    @Override
    public SMTPClientTransport createPlain() {
        return new NettySMTPClientTransport(SMTPDeliveryMode.PLAIN, null, factory);
    }
    

    @Override
    public SMTPClientTransport createSMTPS(SSLContext context) {
        return new NettySMTPClientTransport(SMTPDeliveryMode.SMTPS, context, factory);
    }
    

    @Override
    public SMTPClientTransport createStartTLS(SSLContext context, boolean failOnNoSupport) {
        SMTPDeliveryMode mode;
        if (failOnNoSupport) {
            mode = SMTPDeliveryMode.STARTTLS_DEPEND;
        } else {
            mode = SMTPDeliveryMode.STARTTLS_TRY;
        }
        return new NettySMTPClientTransport(mode, context, factory);
    }
}
