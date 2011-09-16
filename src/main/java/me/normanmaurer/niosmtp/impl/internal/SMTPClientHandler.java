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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.DeliveryRecipientStatus.Status;
import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;
import me.normanmaurer.niosmtp.SMTPState;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPUnsupportedExtensionException;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SimpleChannelUpstreamHandler} implementation which handles the SMTP communication with the SMTP
 * Server
 * 
 * 
 * TODO: The handling of the specific {@link SMTPState} should better be pluggable to make the impl more clean 
 * 
 * 
 * @author Norman Maurer
 * 
 * 
 *
 */
public class SMTPClientHandler extends SimpleChannelUpstreamHandler implements SMTPClientConstants {
    private final Logger logger = LoggerFactory.getLogger(SMTPClientHandler.class);
    private final String mailFrom;
    private final LinkedList<String> recipients;
    private final SMTPClientConfig config;
    private final MessageInput msg;
    private final SMTPClientFutureImpl future;
    private final SMTPStateMachine stateMachine = new SMTPStateMachine();
    
    private final SSLEngine engine;
    private final boolean dependOnStartTLS;;


    public SMTPClientHandler(SMTPClientFutureImpl future, String mailFrom, LinkedList<String> recipients, MessageInput msg, SMTPClientConfig config) {
        this(future, mailFrom, recipients, msg, config, false, null);
    }
    
    public SMTPClientHandler(SMTPClientFutureImpl future, String mailFrom, LinkedList<String> recipients, MessageInput msg, SMTPClientConfig config, boolean dependOnStartTLS, SSLEngine engine) {
        this.mailFrom = mailFrom;
        this.recipients = recipients;
        this.config = config;
        this.msg = msg;
        this.future = future;
        this.dependOnStartTLS = dependOnStartTLS;
        this.engine = engine;
        stateMachine.nextState(SMTPState.EHLO);
        
    }
    

    @Override
    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Map<String, Object> states = new HashMap<String, Object>();
        states.put(RECIPIENT_STATUS_LIST_KEY, new ArrayList<DeliveryRecipientStatus>());
        
        ctx.setAttachment(states);
        super.channelBound(ctx, e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof SMTPResponse) {
            final Map<String, Object> states =  (Map<String, Object>) ctx.getAttachment();
            SMTPResponse response = (SMTPResponse) e.getMessage();
            final List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) states.get(RECIPIENT_STATUS_LIST_KEY);
            
            boolean supportsPipelining =  states.containsKey(SUPPORTS_PIPELINING_KEY);
            boolean supportsStartTLS = states.containsKey(SUPPORTS_STARTTLS_KEY);
            
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
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                }
                break;
            case EHLO:
                if (code < 400) {
                    ctx.getChannel().write(SMTPRequestImpl.ehlo(config.getHeloName()));
                    if (engine == null) {
                        stateMachine.nextState(SMTPState.MAIL);
                    } else {
                        stateMachine.nextState(SMTPState.STARTTLS);
                    }
                } else {
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                }
                break;
            case STARTTLS:
                if (stateMachine.getLastState() == SMTPState.EHLO ) {
                    // Check if the SMTPServer supports PIPELINING 
                    Set<String> extensions = getSupportedExtensions(response);

                    if (extensions.contains(PIPELINING_EXTENSION)) {
                        states.put(SUPPORTS_PIPELINING_KEY, true);
                        supportsPipelining = true;
                    }
                    if (extensions.contains(STARTTLS_EXTENSION)) {
                        states.put(STARTTLS_EXTENSION, true);
                        supportsStartTLS = true;
                    }
                    
                    if (extensions.contains(_8BITMIME_EXTENSION)) {
                        states.put(SUPPORTS_8BITMIME_KEY, true);
                    }
                    
                }

               
                if (code < 400) {
                    if (supportsStartTLS) {
                        ctx.getChannel().write(SMTPRequestImpl.startTls());
                        stateMachine.nextState(SMTPState.MAIL); 
                        break;
                    } else if (!supportsStartTLS && engine != null &&  dependOnStartTLS == true) {
                        throw new SMTPUnsupportedExtensionException("Extension STARTTLS is not supported");
                        
                    } else {
                        stateMachine.nextState(SMTPState.MAIL);
                    }
                } else {
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }

                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                    
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                    break;
                }
                // if we not break yet we will fall back to MAIL
                
            case MAIL:
                
                if (stateMachine.getLastState() == SMTPState.EHLO ) {
                    // Check if the SMTPServer supports PIPELINING 
                    Set<String> extensions = getSupportedExtensions(response);

                    if (extensions.contains(PIPELINING_EXTENSION)) {
                        states.put(SUPPORTS_PIPELINING_KEY, true);
                        supportsPipelining = true;
                    }
                    if (extensions.contains(STARTTLS_EXTENSION)) {
                        states.put(STARTTLS_EXTENSION, true);
                        supportsStartTLS = true;
                    }
                    
                }
                
                
                
                String mail = mailFrom;
               
                // handle null senders
                if (mail == null) {
                    mail = "";
                }
                if (code < 400) {
                    
                    // Check if we depend on pipelining 
                    if (!supportsPipelining && config.getPipeliningMode() == PipeliningMode.DEPEND) {
                        throw new SMTPUnsupportedExtensionException("Extension PIPELINING is not supported");
                    }
                    // Check if we need to add the SslHandler for STARTTLS
                    if (stateMachine.getLastState() == SMTPState.STARTTLS) {
                        SslHandler sslHandler =  new SslHandler(engine, false);
                        ctx.getChannel().getPipeline().addFirst("sslHandler", sslHandler);
                        sslHandler.handshake();

                    }
                    // We use a SMTPPipelinedRequest if the SMTPServer supports PIPELINING. This will allow the NETTY to get
                    // the MAX throughput as the encoder will write it out in one buffer if possible. This result in less system calls
                    if (supportsPipelining) {
                        SMTPPipelinedRequestImpl request = new SMTPPipelinedRequestImpl();
                        request.add(SMTPRequestImpl.mail(mail));
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
                    
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                }
                break;
            case RCPT:
                if (stateMachine.getLastState() == SMTPState.RCPT) {
                    statusList.add(new DeliveryRecipientStatusImpl((String) states.remove(LAST_RECIPIENT_KEY), response));
                } else if (code > 400) {
                    while (!recipients.isEmpty()) {
                        statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                    }

                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                    
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                    break;
                }
                
                if (code < 400 || stateMachine.getLastState() == SMTPState.RCPT) {

                    String rcpt = recipients.removeFirst();
                    states.put(LAST_RECIPIENT_KEY, rcpt);
                    
                    // only write the request if the SMTPServer does not support PIPELINING and we don't want to use it
                    // as otherwise we already sent this 
                    if (!supportsPipelining || config.getPipeliningMode() == PipeliningMode.NO) {
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
                    if (!supportsPipelining || config.getPipeliningMode() == PipeliningMode.NO) {
                        ctx.getChannel().write(SMTPRequestImpl.data());
                    }
                    stateMachine.nextState(SMTPState.DATA_POST);

                } else {
                    
                    // all recipients failed so we should now complete the future
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                    
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                }

                break;
            case DATA_POST:
                if (code < 400) {
                    InputStream msgIn;
                    if (states.containsKey(SUPPORTS_8BITMIME_KEY)) {
                        msgIn = msg.get8Bit();
                    } else {
                        msgIn = msg.get7bit();
                    }
                    ctx.getChannel().write(new ChunkedStream(new DataTerminatingInputStream(msgIn)));
                    stateMachine.nextState(SMTPState.QUIT);
                } else {
                    Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                    while(status.hasNext()) {
                        ((DeliveryRecipientStatusImpl)status.next()).setResponse(response);
                    }
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);

                }
                break;
            case QUIT:
                if (code < 400) {
                   
                    // Set the final status for successful recipients
                    Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                    while(status.hasNext()) {
                        DeliveryRecipientStatus s = status.next();
                        if (s.getStatus() == Status.Ok) {
                            ((DeliveryRecipientStatusImpl)s).setResponse(response);
                        }
                    }
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                    
                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);


                } else {
                    Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                    while(status.hasNext()) {
                        ((DeliveryRecipientStatusImpl)status.next()).setResponse(response);
                    }
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                    ctx.getChannel().write(SMTPRequestImpl.quit()).addListener(ChannelFutureListener.CLOSE);
                }
                break;
            default:
                ctx.getChannel().write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                break;
            }

        }
        super.messageReceived(ctx, e);
    }

    private Set<String> getSupportedExtensions(SMTPResponse response) {
        Set<String> extensions = new HashSet<String>();
        Iterator<String> lines = response.getLines().iterator();
        while(lines.hasNext()) {
            String line = lines.next();
            if (line.equalsIgnoreCase(PIPELINING_EXTENSION)) {
                extensions.add(SUPPORTS_PIPELINING_KEY);
            } else if (line.equalsIgnoreCase(STARTTLS_EXTENSION)) {
                extensions.add(STARTTLS_EXTENSION);
            }
        }
        return extensions;
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Exception caught while handle SMTP/SMTPS", e.getCause());
        }
        e.getCause().printStackTrace();
                
        if (!future.isDone()) {
            future.setDeliveryStatus(DeliveryResultImpl.create(e.getCause()));
        }
        if (ctx.getChannel().isConnected()) {
            ctx.getChannel().write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

    }

    

}
