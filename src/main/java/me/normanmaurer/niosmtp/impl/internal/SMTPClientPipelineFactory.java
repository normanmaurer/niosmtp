package me.normanmaurer.niosmtp.impl.internal;

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

    private final static DelimiterBasedFrameDecoder FRAMER = new DelimiterBasedFrameDecoder(8192, false, Delimiters.lineDelimiter());
    private final static SMTPResponseDecoder SMTP_RESPONSE_DECODER = new SMTPResponseDecoder();
    private final static SMTPRequestEncoder SMTP_REQUEST_ENCODER = new SMTPRequestEncoder();
    private final static SMTPClientHandler SMTP_CLIENT_HANDLER = new SMTPClientHandler();
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("framer", FRAMER);
        pipeline.addLast("decoder", SMTP_RESPONSE_DECODER);
        pipeline.addLast("encoder", SMTP_REQUEST_ENCODER);
        pipeline.addLast("chunk", new ChunkedWriteHandler());
        pipeline.addLast("coreHandler", SMTP_CLIENT_HANDLER);
        return pipeline;
    }

}
