package me.normanmaurer.niosmtp.impl.internal;

import java.nio.charset.Charset;

import me.normanmaurer.niosmtp.SMTPRequest;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OneToOneEncoder} which encoded {@link SMTPRequest} objects to {@link ChannelBuffer}
 * 
 * @author Norman Maurer
 *
 */
class SMTPRequestEncoder extends OneToOneEncoder{
    private final static Charset CHARSET = Charset.forName("US-ASCII");
    private final Logger logger = LoggerFactory.getLogger(SMTPRequestEncoder.class);

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel arg1, Object msg) throws Exception {
        if (msg instanceof SMTPRequest) {
            SMTPRequest req = (SMTPRequest) msg;
            if (logger.isDebugEnabled()) {
                logger.debug("Channel " + ctx.getChannel().getId() + " sent: [" + req.toString() + "]");
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(req.getCommand());
            
            if (req.getArgument() != null) {
                sb.append(" ");
                sb.append(req.getArgument());
            }
            sb.append("\r\n");
            return ChannelBuffers.wrappedBuffer(sb.toString().getBytes(CHARSET));
        }
        return msg;
    }

}
