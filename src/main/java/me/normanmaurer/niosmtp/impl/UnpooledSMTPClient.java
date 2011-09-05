package me.normanmaurer.niosmtp.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
    }
    
    public SMTPClientFuture deliver(InetSocketAddress host, String mailFrom, List<String> recipients, InputStream msg, SMTPClientConfig config) {
        final SMTPClientFutureImpl future = new SMTPClientFutureImpl();
        bootstrap.setPipelineFactory(new SMTPClientPipelineFactory(mailFrom, recipients, msg, config));
        bootstrap.connect(host).addListener(new ChannelFutureListener() {
            
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                Map<String, Object> attrs = new HashMap<String, Object>();
                attrs.put(FUTURE_KEY, future);
                attrs.put(NEXT_COMMAND_KEY, SMTPCommand.HELO);
                ATTRIBUTES.set(cf.getChannel(), attrs);
            }
        });
        return future;
    }

    public void destroy() {
        bootstrap.releaseExternalResources();
    }
    
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        UnpooledSMTPClient client = new UnpooledSMTPClient();
        SMTPClientFuture future = client.deliver(new InetSocketAddress("192.168.0.254", 25), "test@test.de", Arrays.asList("nm2@normanmaurer.me", "nm2@normanmaurer.me"), new ByteArrayInputStream("Subject: test\r\n\r\ntest".getBytes()), new SMTPClientConfigImpl());
        Iterator<DeliveryRecipientStatus> statusIt = future.get();
        while(statusIt.hasNext()) {
            DeliveryRecipientStatus rs = statusIt.next();
            System.out.println(rs.getAddress() + "=> " + rs.getReturnCode() + " " + rs.getResponse());
        }
    }
}
