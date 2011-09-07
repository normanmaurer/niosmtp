package me.normanmaurer.niosmtp.impl.internal;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPCommand;
import me.normanmaurer.niosmtp.SMTPResponse;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.stream.ChunkedStream;

public class SMTPClientHandler extends SimpleChannelUpstreamHandler implements ChannelLocalSupport {


    public SMTPClientHandler() {
    }

    @Override
    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Map<String, Object> states = ATTRIBUTES.get(e.getChannel());
        states.put(NEXT_COMMAND_KEY, SMTPCommand.HELO);

        super.channelBound(ctx, e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof SMTPResponse) {
            final Map<String, Object> states = ATTRIBUTES.get(e.getChannel());
            SMTPResponse response = (SMTPResponse) e.getMessage();
            final SMTPCommand nextCommand = (SMTPCommand) states.get(NEXT_COMMAND_KEY);
            final SMTPCommand curCommand = (SMTPCommand) states.get(CURRENT_COMMAND_KEY);
            final SMTPClientConfig config = (SMTPClientConfig) states.get(SMTP_CONFIG_KEY);
            final LinkedList<String> recipients = (LinkedList<String>) states.get(RECIPIENTS_KEY);

            SMTPClientFutureImpl future = (SMTPClientFutureImpl) ATTRIBUTES.get(e.getChannel()).get(FUTURE_KEY);
            int code = response.getCode();
            switch (nextCommand) {
            case HELO:
                if (code < 400) {
                    ctx.getChannel().write(new SMTPRequestImpl("HELO", config.getHeloName())).addListener(new ChannelFutureListener() {

                        @Override
                        public void operationComplete(ChannelFuture cf) throws Exception {
                            states.put(CURRENT_COMMAND_KEY, nextCommand);

                        }
                    });
                    states.put(NEXT_COMMAND_KEY, SMTPCommand.MAIL);
                } else {
                    setResponseForAll(response, recipients, future, ctx);

                }
                break;
            case MAIL:
                String mailFrom = (String) states.get(MAIL_FROM_KEY);
                if (mailFrom == null) {
                    mailFrom = "";
                }
                if (code < 400) {

                    ctx.getChannel().write(new SMTPRequestImpl("MAIL FROM:", "<" + mailFrom + ">")).addListener(new ChannelFutureListener() {

                        @Override
                        public void operationComplete(ChannelFuture cf) throws Exception {
                            states.put(CURRENT_COMMAND_KEY, nextCommand);

                        }
                    });
                    states.put(NEXT_COMMAND_KEY, SMTPCommand.RCPT);
                } else {
                    setResponseForAll(response, recipients, future, ctx);
                }
                break;
            case RCPT:
                if (curCommand == SMTPCommand.RCPT) {
                    future.addRecipientStatus(new DeliveryRecipientStatusImpl((String) ctx.getAttachment(), response));
                } else if (code > 400) {
                    setResponseForAll(response, recipients, future, ctx);
                    break;
                }
                
                if (code < 400 || curCommand == SMTPCommand.RCPT) {

                    String rcpt = recipients.removeFirst();
                    ctx.setAttachment(rcpt);
                    ctx.getChannel().write(new SMTPRequestImpl("RCPT TO:", "<" + rcpt + ">")).addListener(new ChannelFutureListener() {

                        @Override
                        public void operationComplete(ChannelFuture cf) throws Exception {
                            states.put(CURRENT_COMMAND_KEY, nextCommand);

                        }
                    });
                } 
              
                if (recipients.isEmpty()) {
                    states.put(NEXT_COMMAND_KEY, SMTPCommand.DATA);
                } else {
                    states.put(NEXT_COMMAND_KEY, SMTPCommand.RCPT);
                }
                break;
            case DATA:
                future.addRecipientStatus(new DeliveryRecipientStatusImpl((String) ctx.getAttachment(), response));

                List<DeliveryRecipientStatusImpl> status = future.getStatus();
                boolean success = false;
                for (int i = 0; i < status.size(); i++) {
                   if (status.get(i).isSuccessful()) {
                       success = true;
                       break;
                   }
                }
                if (success) {
                    ctx.getChannel().write(new SMTPRequestImpl("DATA", null)).addListener(new ChannelFutureListener() {

                        @Override
                        public void operationComplete(ChannelFuture cf) throws Exception {
                            states.put(CURRENT_COMMAND_KEY, nextCommand);

                        }
                    });
                    states.put(NEXT_COMMAND_KEY, SMTPCommand.MESSAGE);

                } else {
                    sendQuit(future, ctx);
                }

                break;
            case MESSAGE:
                if (code < 400) {
                    ctx.getChannel().write(new ChunkedStream(new DataTerminatingInputStream((InputStream) states.get(MSG_KEY)))).addListener(new ChannelFutureListener() {

                        @Override
                        public void operationComplete(ChannelFuture cf) throws Exception {
                            states.put(CURRENT_COMMAND_KEY, nextCommand);

                        }
                    });
                    states.put(NEXT_COMMAND_KEY, SMTPCommand.QUIT);
                } else {
                    replaceStatusForAll(response, future, ctx);
                }
                break;
            case QUIT:
                if (code < 400) {
                    sendQuit(future, ctx);
                } else {
                    replaceStatusForAll(response, future, ctx);
                }
                break;
            default:
                ctx.getChannel().write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                break;
            }

        }
        super.messageReceived(ctx, e);
    }
    
    private void setResponseForAll(SMTPResponse response, LinkedList<String> recipients, SMTPClientFutureImpl future, ChannelHandlerContext ctx) {
        while (!recipients.isEmpty()) {
            future.addRecipientStatus(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
        }

        
    }

    private void replaceStatusForAll(SMTPResponse response, SMTPClientFutureImpl future, ChannelHandlerContext ctx) {
        List<DeliveryRecipientStatusImpl> status = future.getStatus();
        for (int i = 0; i < status.size(); i++) {
            status.get(i).setResponse(response);
        }
        sendQuit(future, ctx);


    }
    
    private void sendQuit(SMTPClientFutureImpl future, ChannelHandlerContext ctx) {
        ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);

        future.done();
    }
    

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        SMTPClientFutureImpl future = (SMTPClientFutureImpl) ATTRIBUTES.get(e.getChannel()).get(FUTURE_KEY);
        future.done();
        super.channelClosed(ctx, e);
    }

}
