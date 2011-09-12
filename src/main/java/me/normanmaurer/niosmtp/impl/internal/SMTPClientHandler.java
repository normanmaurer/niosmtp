/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* Selene licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package me.normanmaurer.niosmtp.impl.internal;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.normanmaurer.niosmtp.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.DeliveryRecipientStatus.Status;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPCommand;
import me.normanmaurer.niosmtp.SMTPConnectionException;
import me.normanmaurer.niosmtp.SMTPException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SimpleChannelUpstreamHandler} implementation which handles the SMTP communication with the SMTP
 * Server
 * 
 * @author Norman Maurer
 *
 */
class SMTPClientHandler extends SimpleChannelUpstreamHandler implements ChannelLocalSupport {
    private final Logger logger = LoggerFactory.getLogger(SMTPClientHandler.class);



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
            boolean supportsPipelining =  false;
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
            case EHLO:
                if (code < 400) {
                    ctx.getChannel().write(new SMTPRequestImpl("EHLO", config.getHeloName())).addListener(new ChannelFutureListener() {

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
                
                if (curCommand == SMTPCommand.EHLO) {
                    states.put(SUPPORTS_PIPELINING_KEY, response.getLines().contains("PIPELINING"));
                    supportsPipelining = true;
                }
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
                    states.put(NEXT_COMMAND_KEY, SMTPCommand.DATA_POST);

                } else {
                    ctx.getChannel().write(new SMTPRequestImpl("QUIT", null)).addListener(ChannelFutureListener.CLOSE);
                    
                    // all recipients failed so we should now complete the future
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                }

                break;
            case DATA_POST:
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
                   
                    // Set the final status for successful recipients
                    Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                    while(status.hasNext()) {
                        DeliveryRecipientStatus s = status.next();
                        if (s.getStatus() == Status.Ok) {
                            ((DeliveryRecipientStatusImpl)s).setResponse(response);
                        }
                    }
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
        if (logger.isDebugEnabled()) {
            logger.debug("Exception caught while handle SMTP/SMTPS", e.getCause());
        }
        SMTPClientFutureImpl future = (SMTPClientFutureImpl) ATTRIBUTES.get(e.getChannel()).get(FUTURE_KEY);
        
        if (!future.isDone()) {
            final SMTPException exception;
            final Throwable t = e.getCause();
            if (t instanceof SMTPException) {
                exception = (SMTPException) t;
            } else if (t instanceof ConnectException) {
                exception = new SMTPConnectionException(t);
            } else {
                exception = new SMTPException("Exception while try to deliver msg", t);
            }
            
            future.setDeliveryStatus(new DeliveryResultImpl(exception));
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
