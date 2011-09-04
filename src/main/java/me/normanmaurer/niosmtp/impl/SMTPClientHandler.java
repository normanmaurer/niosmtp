package me.normanmaurer.niosmtp.impl;

import java.io.InputStream;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPCommand;
import me.normanmaurer.niosmtp.SMTPResponse;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.stream.ChunkedStream;

public class SMTPClientHandler extends SimpleChannelUpstreamHandler {

    private SMTPClientConfig config;
    private String mailFrom;
    private InputStream msg;
    private List<String> recipients;

    public SMTPClientHandler(String mailFrom, List<String> recipients, InputStream msg, SMTPClientConfig config) {
        this.config = config;
        this.mailFrom = mailFrom;
        this.recipients = recipients;
        this.msg = msg;
    }

    @Override
    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.setAttachment(SMTPCommand.HELO);

        super.channelBound(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof SMTPResponse) {
            SMTPResponse response = (SMTPResponse) e.getMessage();
            SMTPCommand nextCommand = (SMTPCommand) ctx.getAttachment();
            System.out.println(nextCommand.name());
            int code = response.getCode();
            if (code > 400) {
            } else {
                switch (nextCommand) {
                case HELO:
                    ctx.getChannel().write(new SMTPRequestImpl("HELO", config.getHeloName()));
                    ctx.setAttachment(SMTPCommand.MAIL);
                    break;
                case MAIL:
                    ctx.getChannel().write(new SMTPRequestImpl("MAIL FROM:", "<" + mailFrom + ">"));
                    ctx.setAttachment(SMTPCommand.RCPT);
                    break;
                case RCPT:

                    for (int i = 0; i < recipients.size(); i++) {
                        ctx.getChannel().write(new SMTPRequestImpl("RCPT TO:", "<" + recipients.get(i) + ">"));
                    }
                    ctx.setAttachment(SMTPCommand.DATA);
                    break;
                case DATA:
                    ctx.getChannel().write(new SMTPRequestImpl("DATA", null));
                    ctx.setAttachment(SMTPCommand.MESSAGE);
                    break;
                case MESSAGE:
                    ctx.getChannel().write(new ChunkedStream(msg));
                    ctx.getChannel().write(ChannelBuffers.wrappedBuffer(".\r\n".getBytes()));
                    ctx.setAttachment(SMTPCommand.QUIT);

                case QUIT:
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);
                default:
                    // TODO: fix me
                    break;
                }
            }

        }
        // TODO Auto-generated method stub
        super.messageReceived(ctx, e);
    }

}
