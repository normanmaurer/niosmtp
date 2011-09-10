package me.normanmaurer.niosmtp.impl;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import me.normanmaurer.niosmtp.SMTPClient;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPCommand;
import me.normanmaurer.niosmtp.impl.internal.ChannelLocalSupport;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientPipelineFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

/**
 * {@link SMTPClient} implementation which will create a new Connection for
 * every
 * {@link #deliver(InetSocketAddress, String, List, InputStream, SMTPClientConfig)}
 * call.
 * 
 * So no pooling is active
 * 
 * @author Norman Maurer
 * 
 */
public class UnpooledSMTPClient implements SMTPClient, ChannelLocalSupport {

    protected final NioClientSocketChannelFactory socketFactory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
    private final Timer timer = new HashedWheelTimer();

    protected ChannelPipelineFactory createChannelPipelineFactory() {
        return new SMTPClientPipelineFactory();
    }
    
    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClient#deliver(java.net.InetSocketAddress, java.lang.String, java.util.List, java.io.InputStream, me.normanmaurer.niosmtp.SMTPClientConfig)
     */
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final List<String> recipients, final InputStream msg, final SMTPClientConfig config) {
        final SMTPClientFutureImpl future = new SMTPClientFutureImpl();
        ClientBootstrap bootstrap = new ClientBootstrap(socketFactory);
        bootstrap.setOption("connectTimeoutMillis", config.getConnectionTimeout() * 1000);
        bootstrap.setPipelineFactory(createChannelPipelineFactory());
        InetSocketAddress local = config.getLocalAddress();
        bootstrap.connect(host, local).addListener(new ChannelFutureListener() {
            
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                if (cf.isSuccess()) {
                    Map<String, Object> attrs = new HashMap<String, Object>();
                    attrs.put(FUTURE_KEY, future);
                    attrs.put(NEXT_COMMAND_KEY, SMTPCommand.HELO);
                    attrs.put(MAIL_FROM_KEY, mailFrom);
                    attrs.put(RECIPIENTS_KEY, new LinkedList<String>(recipients));
                    attrs.put(MSG_KEY, msg);
                    attrs.put(SMTP_CONFIG_KEY, config);
                    ATTRIBUTES.set(cf.getChannel(), attrs);
                    
                    // Add the idle timeout handler
                    cf.getChannel().getPipeline().addFirst("idleHandler", new IdleStateHandler(timer, 0, 0, config.getResponseTimeout()));
                }
                // Set the channel so we can close it for cancel later
                future.setChannel(cf.getChannel());
            }
        });
        return future;
    }

    /**
     * Call this method to destroy the {@link SMTPClient} and release all resources
     */
    public void destroy() {
        socketFactory.releaseExternalResources();
        timer.stop();
    }
}
