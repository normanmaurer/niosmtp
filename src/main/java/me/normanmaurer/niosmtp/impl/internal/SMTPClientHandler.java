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
import me.normanmaurer.niosmtp.SMTPState;
import me.normanmaurer.niosmtp.SMTPConnectionException;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;

import org.jboss.netty.buffer.ChannelBuffers;
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
public class SMTPClientHandler extends SimpleChannelUpstreamHandler implements SMTPClientConstants {
    private final Logger logger = LoggerFactory.getLogger(SMTPClientHandler.class);



    public SMTPClientHandler() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Map<String, Object> states = (Map<String, Object>) ctx.getAttachment();
        states.put(RECIPIENT_STATUS_LIST_KEY, new ArrayList<DeliveryRecipientStatus>());
        super.channelBound(ctx, e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof SMTPResponse) {
            final Map<String, Object> states =  (Map<String, Object>) ctx.getAttachment();
            SMTPResponse response = (SMTPResponse) e.getMessage();
            final SMTPStateMachine stateMachine = (SMTPStateMachine) states.get(SMTP_STATE_KEY);
            final SMTPClientConfig config = (SMTPClientConfig) states.get(SMTP_CONFIG_KEY);
            final LinkedList<String> recipients = (LinkedList<String>) states.get(RECIPIENTS_KEY);
            final List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) states.get(RECIPIENT_STATUS_LIST_KEY);
            
            SMTPClientFutureImpl future = (SMTPClientFutureImpl) states.get(FUTURE_KEY);
            boolean supportsPipelining =  states.containsKey(SUPPORTS_PIPELINING_KEY);
            
            
            int code = response.getCode();
            switch (stateMachine.getNextState()) {
            case HELO:
                if (code < 400) {
                    ctx.getChannel().write(SMTPRequestImpl.helo(config.getHeloName()));
                    stateMachine.nextState(SMTPState.MAIL);
                } else {
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                }
                break;
            case EHLO:
                if (code < 400) {
                    ctx.getChannel().write(SMTPRequestImpl.ehlo(config.getHeloName()));
                    stateMachine.nextState(SMTPState.MAIL);
                } else {
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                }
                break;
            case MAIL:
                
                // Check if we need to check for PIPELINING support
                if (stateMachine.getLastState() == SMTPState.EHLO && config.usePipelining()) {
                    // Check if the SMTPServer supports PIPELINING 
                    Iterator<String> lines = response.getLines().iterator();
                    while(lines.hasNext()) {
                        if (lines.next().equalsIgnoreCase(PIPELINING_EXTENSION)) {
                            states.put(SUPPORTS_PIPELINING_KEY, true);
                            supportsPipelining = true;
                            break;
                        }
                    }
                }
                String mailFrom = (String) states.get(MAIL_FROM_KEY);
               
                // handle null senders
                if (mailFrom == null) {
                    mailFrom = "";
                }
                if (code < 400) {

                    // We use a SMTPPipelinedRequest if the SMTPServer supports PIPELINING. This will allow the NETTY to get
                    // the MAX throughput as the encoder will write it out in one buffer if possible. This result in less system calls
                    if (supportsPipelining) {
                        SMTPPipelinedRequestImpl request = new SMTPPipelinedRequestImpl();
                        request.add(SMTPRequestImpl.mail(mailFrom));
                        for (int i = 0; i < recipients.size(); i++) {
                            request.add(SMTPRequestImpl.rcpt(recipients.get(i)));
                        }
                        request.add(SMTPRequestImpl.data());
                        ctx.getChannel().write(request);

                    } else {
                        ctx.getChannel().write(SMTPRequestImpl.mail(mailFrom));
                    }
                    stateMachine.nextState(SMTPState.RCPT);

                } else {
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                }
                break;
            case RCPT:
                if (stateMachine.getLastState() == SMTPState.RCPT) {
                    statusList.add(new DeliveryRecipientStatusImpl((String) states.remove(LAST_RECIPIENT_KEY), response));
                } else if (code > 400) {
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                    break;
                }
                
                if (code < 400 || stateMachine.getLastState() == SMTPState.RCPT) {

                    String rcpt = recipients.removeFirst();
                    states.put(LAST_RECIPIENT_KEY, rcpt);
                    
                    // only write the request if the SMTPServer does not support PIPELINING and we don't want to use it
                    // as otherwise we already sent this 
                    if (!supportsPipelining || !config.usePipelining()) {
                        ctx.getChannel().write(SMTPRequestImpl.rcpt(rcpt));
                    }
                    
                } 
              
                if (recipients.isEmpty()) {
                    stateMachine.nextState(SMTPState.DATA);
                } else {
                    stateMachine.nextState(SMTPState.RCPT);
                }
                break;
            case DATA:
                statusList.add(new DeliveryRecipientStatusImpl((String) states.remove(LAST_RECIPIENT_KEY), response));

                boolean success = false;
                for (int i = 0; i < statusList.size(); i++) {
                   if (statusList.get(i).getStatus() == Status.Ok) {
                       success = true;
                       break;
                   }
                }
                if (success) {
                    // only write the request if the SMTPServer does not support PIPELINING and we don't want to use it
                    // as otherwise we already sent this 
                    if (!supportsPipelining || !config.usePipelining()) {
                        ctx.getChannel().write(SMTPRequestImpl.data());
                    }
                    stateMachine.nextState(SMTPState.DATA_POST);

                } else {
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);
                    
                    // all recipients failed so we should now complete the future
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                }

                break;
            case DATA_POST:
                if (code < 400) {
                    ctx.getChannel().write(new ChunkedStream(new DataTerminatingInputStream((InputStream) states.get(MSG_KEY))));
                    stateMachine.nextState(SMTPState.QUIT);
                } else {
                    Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                    while(status.hasNext()) {
                        ((DeliveryRecipientStatusImpl)status.next()).setResponse(response);
                    }
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                }
                break;
            case QUIT:
                if (code < 400) {
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);
                   
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
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);
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

    @SuppressWarnings("unchecked")
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Exception caught while handle SMTP/SMTPS", e.getCause());
        }
        e.getCause().printStackTrace();
        SMTPClientFutureImpl future = (SMTPClientFutureImpl) ((Map<String, Object>) ctx.getAttachment()).get(FUTURE_KEY);
        
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

    

}
