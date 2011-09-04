package me.normanmaurer.niosmtp.impl;

import me.normanmaurer.niosmtp.SMTPRequest;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class SMTPRequestEncoder extends OneToOneEncoder{

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
            return sb.toString();
        }
        return msg;
    }

}
