package me.normanmaurer.niosmtp.impl.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.normanmaurer.niosmtp.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.DeliveryRecipientStatus.Status;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPCommand;
import me.normanmaurer.niosmtp.SMTPResponse;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.stream.ChunkedStream;

/**
 * {@link SimpleChannelUpstreamHandler} implementation which handles the SMTP communication with the SMTP
 * Server
 * 
 * @author Norman Maurer
 *
 */
class SMTPClientHandler extends SimpleChannelUpstreamHandler implements ChannelLocalSupport {



    public SMTPClientHandler() {
    }

    @Override
    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Map<String, Object> states = ATTRIBUTES.get(e.getChannel());
        states.put(NEXT_COMMAND_KEY, SMTPCommand.HELO);
        states.put(RECIPIENT_STATUS_LIST_KEY, new ArrayList<DeliveryRecipientStatus>());
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
            final List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) states.get(RECIPIENT_STATUS_LIST_KEY);
            
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
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);

                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

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
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                }
                break;
            case RCPT:
                if (curCommand == SMTPCommand.RCPT) {
                    statusList.add(new DeliveryRecipientStatusImpl((String) ctx.getAttachment(), response));
                } else if (code > 400) {
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);

                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
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
                statusList.add(new DeliveryRecipientStatusImpl((String) ctx.getAttachment(), response));

                boolean success = false;
                for (int i = 0; i < statusList.size(); i++) {
                   if (statusList.get(i).getStatus() == Status.Ok) {
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
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);
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
                    Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                    while(status.hasNext()) {
                        ((DeliveryRecipientStatusImpl)status.next()).setResponse(response);
                    }
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                }
                break;
            case QUIT:
                if (code < 400) {
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                } else {
                    Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                    while(status.hasNext()) {
                        ((DeliveryRecipientStatusImpl)status.next()).setResponse(response);
                    }
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                }
                break;
            default:
                ctx.getChannel().write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                break;
            }

        }
        super.messageReceived(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        SMTPClientFutureImpl future = (SMTPClientFutureImpl) ATTRIBUTES.get(e.getChannel()).get(FUTURE_KEY);
        if (!future.isDone()) {
            future.setDeliveryStatus(new DeliveryResultImpl(e.getCause()));
        }
        if (ctx.getChannel().isConnected()) {
            ctx.getChannel().write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);
        ATTRIBUTES.remove(ctx.getChannel());
    }
    

}
