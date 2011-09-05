package me.normanmaurer.niosmtp.impl.internal;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPClientConfig;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

public class SMTPClientPipelineFactory implements ChannelPipelineFactory{

    private final String mailFrom;
    private final List<String> recipients;
    private final InputStream msg;
    private final SMTPClientConfig config;
    private final static Charset CHARSET = Charset.forName("US-ASCII");
    
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
        pipeline.addLast("stringDecoder", new StringDecoder(CHARSET));
        pipeline.addLast("decoder", new SMTPResponseDecoder());
        pipeline.addLast("stringEncoder", new StringEncoder(CHARSET));
        pipeline.addLast("encoder", new SMTPRequestEncoder());
        pipeline.addLast("chunk", new ChunkedWriteHandler());
        pipeline.addLast("coreHandler", new SMTPClientHandler(mailFrom, recipients, msg, config));
        return pipeline;
    }

}
