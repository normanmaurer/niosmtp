package me.normanmaurer.niosmtp.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import me.normanmaurer.niosmtp.RecipientStatus;
import me.normanmaurer.niosmtp.SMTPClient;
import me.normanmaurer.niosmtp.SMTPClientConfig;

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
public class UnpooledSMTPClient implements SMTPClient {

    private final ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
    
    public UnpooledSMTPClient() {
    }
    
    public Future<List<RecipientStatus>> deliver(InetSocketAddress host, String mailFrom, List<String> recipients, InputStream msg, SMTPClientConfig config) {
        bootstrap.setPipelineFactory(new SMTPClientPipelineFactory(mailFrom, recipients, msg, config));

        bootstrap.connect(host);
        // TODO Auto-generated method stub
        return null;
    }

    public void destroy() {
        bootstrap.releaseExternalResources();
    }
    
    public static void main(String[] args) {
        UnpooledSMTPClient client = new UnpooledSMTPClient();
        client.deliver(new InetSocketAddress("192.168.0.254", 25), "test@test.de", Arrays.asList("nm@normanmaurer.me"), new ByteArrayInputStream("Subject: test\r\n\r\ntest".getBytes()), new SMTPClientConfigImpl());
    }
}
