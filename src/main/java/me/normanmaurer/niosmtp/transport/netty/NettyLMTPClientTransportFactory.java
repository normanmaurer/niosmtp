package me.normanmaurer.niosmtp.transport.netty;

import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransportFactory;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;

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
    
    public NettyLMTPClientTransportFactory(ClientSocketChannelFactory factory, SMTPClientSessionFactory sessionFactory) {
        super(factory, sessionFactory);
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
    
}
