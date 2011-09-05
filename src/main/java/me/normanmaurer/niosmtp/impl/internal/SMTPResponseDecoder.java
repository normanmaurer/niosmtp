package me.normanmaurer.niosmtp.impl.internal;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

class SMTPResponseDecoder extends OneToOneDecoder{


    @Override
    protected Object decode(ChannelHandlerContext arg0, Channel arg1, Object msg) throws Exception {
        String parts[] = ((String)msg).split(" ", 2);
        if (parts.length == 0) {
            return null;
        } else {
            return new SMTPResponseImpl(Integer.parseInt(parts[0]), parts[1]);
        }
    }

}
