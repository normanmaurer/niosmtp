package me.normanmaurer.niosmtp.impl.internal;

import java.nio.charset.Charset;

import me.normanmaurer.niosmtp.SMTPResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

/**
 * {@link OneToOneDecoder} which decodes {@link SMTPResponse}'s. It also handles
 * multi-line responses.
 * 
 * 
 * @author Norman Maurer
 * 
 */
class SMTPResponseDecoder extends OneToOneDecoder {
    private final static Charset CHARSET = Charset.forName("US-ASCII");

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof ChannelBuffer) {
            ChannelBuffer line = (ChannelBuffer) msg;

            SMTPResponseImpl response = (SMTPResponseImpl) ctx.getAttachment();

            // The separator must be on index 3 as the return code has always 3
            // digits
            int separator = line.getByte(3);

            if (separator == ' ') {
                // Ok we had a ' ' separator which means this was the end of the
                // SMTPResponse
                if (response == null) {
                    int code = Integer.parseInt(line.readBytes(3).toString(CHARSET));
                    response = new SMTPResponseImpl(code);
                }
                if (line.readable()) {
                    response.addLine(line.toString(CHARSET));

                }
                ctx.setAttachment(null);

                return response;
            } else if (separator == '-') {
                // The '-' separator is used for multi-line responses so just
                // add it to the response
                if (response == null) {
                    int code = Integer.parseInt(line.readBytes(3).toString(CHARSET));
                    response = new SMTPResponseImpl(code);
                    ctx.setAttachment(response);

                }
                if (line.readable()) {
                    response.addLine(line.toString(CHARSET));
                }
            }

            return null;
        } else {
            return msg;
        }

    }

}
