package me.normanmaurer.niosmtp.impl;

import java.io.InputStream;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPClientConfig;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;

public class SMTPClientPipelineFactory implements ChannelPipelineFactory{

    private final String mailFrom;
    private final List<String> recipients;
    private final InputStream msg;
    private final SMTPClientConfig config;
    
    public SMTPClientPipelineFactory(String mailFrom, List<String> recipients, InputStream msg, SMTPClientConfig config) {
        this.mailFrom = mailFrom;
        this.recipients = recipients;
        this.msg = msg;
        this.config = config;
        
    }
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, true, Delimiters.lineDelimiter()));
        pipeline.addLast("coreHandler", new SMTPClientHandler(mailFrom, recipients, msg, config));
        return pipeline;
    }

}
