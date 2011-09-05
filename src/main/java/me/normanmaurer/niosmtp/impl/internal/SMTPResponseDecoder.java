package me.normanmaurer.niosmtp.impl.internal;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

class SMTPResponseDecoder extends OneToOneDecoder {
    private final static Charset CHARSET = Charset.forName("US-ASCII");

    @Override
    protected Object decode(ChannelHandlerContext arg0, Channel arg1, Object msg) throws Exception {
        if (msg instanceof ChannelBuffer) {
            String parts[] = ((ChannelBuffer) msg).toString(CHARSET).split(" ", 2);
            if (parts.length == 0) {
                return null;
            } else {
                return new SMTPResponseImpl(Integer.parseInt(parts[0]), parts[1]);
            }
        }
        return msg;
    }

}
