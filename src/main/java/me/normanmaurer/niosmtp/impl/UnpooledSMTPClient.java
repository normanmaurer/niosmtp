package me.normanmaurer.niosmtp.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import me.normanmaurer.niosmtp.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.SMTPClient;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPCommand;
import me.normanmaurer.niosmtp.impl.internal.ChannelLocalSupport;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientConfigImpl;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientPipelineFactory;

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

    private final ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
    
    public UnpooledSMTPClient() {
        bootstrap.setPipelineFactory(new SMTPClientPipelineFactory());
    }
    
    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClient#deliver(java.net.InetSocketAddress, java.lang.String, java.util.List, java.io.InputStream, me.normanmaurer.niosmtp.SMTPClientConfig)
     */
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final List<String> recipients, final InputStream msg, final SMTPClientConfig config) {
        final SMTPClientFutureImpl future = new SMTPClientFutureImpl();
        bootstrap.connect(host).addListener(new ChannelFutureListener() {
            
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                Map<String, Object> attrs = new HashMap<String, Object>();
                attrs.put(FUTURE_KEY, future);
                attrs.put(NEXT_COMMAND_KEY, SMTPCommand.HELO);
                attrs.put(MAIL_FROM_KEY, mailFrom);
                attrs.put(RECIPIENTS_KEY, new LinkedList<String>(recipients));
                attrs.put(MSG_KEY, msg);
                attrs.put(SMTP_CONFIG_KEY, config);
                ATTRIBUTES.set(cf.getChannel(), attrs);
                
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
        bootstrap.releaseExternalResources();
    }
    
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        UnpooledSMTPClient client = new UnpooledSMTPClient();
        SMTPClientFuture future = client.deliver(new InetSocketAddress("192.168.0.254", 25), "test@test.de", Arrays.asList("nm@normanmaurer.me", "nm2@normanmaurer.me"), new ByteArrayInputStream("Subject: test\r\n\r\ntest".getBytes()), new SMTPClientConfigImpl());
        Iterator<DeliveryRecipientStatus> statusIt = future.get();
        while(statusIt.hasNext()) {
            DeliveryRecipientStatus rs = statusIt.next();
            System.out.println(rs.getAddress() + "=> " + rs.getResponse().getCode() + " " + rs.getResponse().getLines().toString());
        }
    }
}
