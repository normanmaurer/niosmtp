package me.normanmaurer.niosmtp.impl;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;

import javax.net.ssl.SSLContext;

import me.normanmaurer.niosmtp.SMTPClient;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.impl.internal.SMTPSClientPipelineFactory;

import org.jboss.netty.channel.ChannelPipelineFactory;

/**
 * {@link SMTPClient} implementation which will create a new Connection for
 * every {@link #deliver(InetSocketAddress, String, List, InputStream, SMTPClientConfig)}
 * call and use SMTPS for the delivery
 * 
 * So no pooling is active
 * 
 * @author Norman Maurer
 * 
 */
public class UnpooledSMTPSClient extends UnpooledSMTPClient{

    private final SSLContext context;

    public UnpooledSMTPSClient(SSLContext context) {
        this.context = context;
    }

    @Override
    protected ChannelPipelineFactory createChannelPipelineFactory() {
        return new SMTPSClientPipelineFactory(context);
    }
    
}
