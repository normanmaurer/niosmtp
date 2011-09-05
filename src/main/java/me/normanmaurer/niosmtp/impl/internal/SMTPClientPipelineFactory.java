package me.normanmaurer.niosmtp.impl.internal;

import java.io.InputStream;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPClientConfig;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

/**
 * {@link ChannelPipelineFactory} which is used for the SMTP Client
 * 
 * @author Norman Maurer
 * 
 *
 */
public class SMTPClientPipelineFactory implements ChannelPipelineFactory{

    private final static DelimiterBasedFrameDecoder FRAMER = new DelimiterBasedFrameDecoder(8192, true, Delimiters.lineDelimiter());
    private final static SMTPResponseDecoder SMTP_RESPONSE_DECODER = new SMTPResponseDecoder();
    private final static SMTPRequestEncoder SMTP_REQUEST_ENCODER = new SMTPRequestEncoder();
    
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
        pipeline.addLast("framer", FRAMER);
        pipeline.addLast("decoder", SMTP_RESPONSE_DECODER);
        pipeline.addLast("encoder", SMTP_REQUEST_ENCODER);
        pipeline.addLast("chunk", new ChunkedWriteHandler());
        pipeline.addLast("coreHandler", new SMTPClientHandler(mailFrom, recipients, msg, config));
        return pipeline;
    }

}
