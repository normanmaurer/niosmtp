package me.normanmaurer.niosmtp.impl;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPCommand;
import me.normanmaurer.niosmtp.SMTPResponse;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.stream.ChunkedStream;

public class SMTPClientHandler extends SimpleChannelUpstreamHandler implements ChannelLocalSupport {

    private SMTPClientConfig config;
    private String mailFrom;
    private InputStream msg;
    private LinkedList<String> recipients;

    public SMTPClientHandler(String mailFrom, List<String> recipients, InputStream msg, SMTPClientConfig config) {
        this.config = config;
        this.mailFrom = mailFrom;
        this.recipients = new LinkedList<String>(recipients);
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
            int code = response.getCode();
            if (code > 400) {
                
            } else {
                switch (nextCommand) {
                case HELO:
                    ctx.getChannel().write(new SMTPRequestImpl("HELO", config.getHeloName()));
                    ctx.setAttachment(SMTPCommand.MAIL);
                    break;
                case MAIL:
                    if (mailFrom == null) {
                        mailFrom = "";
                    }
                    ctx.getChannel().write(new SMTPRequestImpl("MAIL FROM:", "<" + mailFrom + ">"));
                    ctx.setAttachment(SMTPCommand.RCPT);
                    break;
                case RCPT:
                    String rcpt = recipients.removeFirst();
                    ctx.getChannel().write(new SMTPRequestImpl("RCPT TO:", "<" + rcpt + ">"));
                    if (recipients.isEmpty()) {
                        ctx.setAttachment(SMTPCommand.DATA);
                    } else {
                        ctx.setAttachment(SMTPCommand.RCPT);
                    }
                    break;
                case DATA:
                    ctx.getChannel().write(new SMTPRequestImpl("DATA", null));
                    ctx.setAttachment(SMTPCommand.MESSAGE);
                    break;
                case MESSAGE:
                    ctx.getChannel().write(new ChunkedStream(msg));
                    ctx.getChannel().write(ChannelBuffers.wrappedBuffer("\r\n.\r\n".getBytes()));
                    ctx.setAttachment(SMTPCommand.QUIT);
                    break;
                case QUIT:
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);
                    break;
                default:
                    ctx.getChannel().write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                    break;
                }
            }

        }
        super.messageReceived(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        SMTPClientFutureImpl future = (SMTPClientFutureImpl) ATTRIBUTES.get(e.getChannel()).get(FUTURE_KEY);
        future.done();
        super.channelClosed(ctx, e);
    }

}
