package me.normanmaurer.niosmtp.impl.internal;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.ssl.SslHandler;

/**
 * {@link ChannelPipelineFactory} which is used for SMTPS connections
 * 
 * @author Norman Maurer
 * 
 *
 */
public class SMTPSClientPipelineFactory extends SMTPClientPipelineFactory{

    private final SSLContext context;

    public SMTPSClientPipelineFactory(SSLContext context) {
        this.context = context;
    }
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline cp = super.getPipeline();
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);
        SslHandler sslHandler = new SslHandler(engine);
        cp.addFirst("sslHandler", sslHandler);
        return cp;
    }

}
