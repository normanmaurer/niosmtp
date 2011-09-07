package me.normanmaurer.niosmtp.impl.internal;

import java.nio.charset.Charset;

import me.normanmaurer.niosmtp.SMTPRequest;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * {@link OneToOneEncoder} which encoded {@link SMTPRequest} objects to {@link ChannelBuffer}
 * 
 * @author Norman Maurer
 *
 */
class SMTPRequestEncoder extends OneToOneEncoder{
    private final static Charset CHARSET = Charset.forName("US-ASCII");

    @Override
    protected Object encode(ChannelHandlerContext arg0, Channel arg1, Object msg) throws Exception {
        if (msg instanceof SMTPRequest) {
            SMTPRequest req = (SMTPRequest) msg;
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
