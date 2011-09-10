package me.normanmaurer.niosmtp.impl.internal;

import me.normanmaurer.niosmtp.SMTPIdleException;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;


public class SMTPClientIdleHandler extends IdleStateAwareChannelUpstreamHandler{

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        if (e.getState() == IdleState.ALL_IDLE) {
            throw new SMTPIdleException();
        }
        super.channelIdle(ctx, e);
    }

}
