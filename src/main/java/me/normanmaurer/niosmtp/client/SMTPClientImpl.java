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
package me.normanmaurer.niosmtp.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientConstants;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPUnsupportedExtensionException;
import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus.Status;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.impl.internal.NettySMTPClientFuture;



/**
 * {@link SMTPClientImpl} which use the wrapped {@link SMTPClientTransport} to deliver email
 * 
 * So no pooling is active
 * 
 * @author Norman Maurer
 * 
 */
public class SMTPClientImpl implements SMTPClientConstants,SMTPClient {
    private final static String PIPELINING_ACTIVE_KEY = "PIPELINING_ACTIVE";

    private SMTPClientTransport transport;

    public SMTPClientImpl(SMTPClientTransport transport) {
        this.transport = transport;
    }

    
    


    @Override
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final Collection<String> recipients, final MessageInput msg, final SMTPClientConfig config) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient must be given");
        }
        
        LinkedList<String> rcpts = new LinkedList<String>(recipients);
        final NettySMTPClientFuture future = new NettySMTPClientFuture();

        transport.connect(host, config,new WelcomeResponseCallback(future, mailFrom, rcpts, msg, config));
        
       
        return future;
    }


    
    private final class WelcomeResponseCallback extends AbstractResponseCallback {

        private SMTPClientConfig config;
        private LinkedList<String> recipients;
        private List<DeliveryRecipientStatus> statusList = new ArrayList<DeliveryRecipientStatus> ();
        private String mailFrom;
        private MessageInput msg;
        
        public WelcomeResponseCallback(SMTPClientFuture future, final String mailFrom, final LinkedList<String> recipients, final MessageInput msg,  final SMTPClientConfig config) {
            super(future);
            this.config = config;
            this.recipients = recipients;
            this.msg = msg;
            this.mailFrom = mailFrom;
        }
        
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            int code = response.getCode();
            if (code < 400) {
                session.send(SMTPRequestImpl.ehlo(config.getHeloName()), new EhloResponseCallback(future, statusList, mailFrom, recipients, msg, config));
/*
                if (context == null) {
                } else {
                    // stateMachine.nextState(SMTPState.STARTTLS);

                }
                */
            } else {
                while (!recipients.isEmpty()) {
                    statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                }
                future.setDeliveryStatus(new DeliveryResultImpl(statusList));

                session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                session.close();

            }            
        }
        
    }
    
    private final class EhloResponseCallback extends AbstractResponseCallback {
        private SMTPClientConfig config;
        private LinkedList<String> recipients;
        private List<DeliveryRecipientStatus> statusList;
        private String mailFrom;
        private MessageInput msg;
        
        public EhloResponseCallback(SMTPClientFuture future, final List<DeliveryRecipientStatus> statusList, final String mailFrom, final LinkedList<String> recipients, final MessageInput msg,  final SMTPClientConfig config) {
            super(future);

            this.config = config;
            this.recipients = recipients;
            this.msg = msg;
            this.mailFrom = mailFrom;
            this.statusList = statusList;
        }
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            boolean supportsPipelining = false;
            boolean supportsStartTLS = false;
            // Check if the SMTPServer supports PIPELINING 
            Set<String> extensions = getSupportedExtensions(response);
            session.setSupportedExtensions(extensions);

            if (extensions.contains(PIPELINING_EXTENSION)) {
                supportsPipelining = true;
            }
            if (extensions.contains(STARTTLS_EXTENSION)) {
                supportsStartTLS = true;
            }
            
            
            
            int code = response.getCode();

            String mail = mailFrom;
           
            // handle null senders
            if (mail == null) {
                mail = "";
            }
            if (code < 400) {
                
                // Check if we depend on pipelining 
                if (!supportsPipelining && config.getPipeliningMode() == PipeliningMode.DEPEND) {
                    future.setDeliveryStatus(DeliveryResultImpl.create(new SMTPUnsupportedExtensionException("Extension PIPELINING is not supported")));
                    session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                    session.close();
                    return;
                }
                /*
                // Check if we need to add the SslHandler for STARTTLS
                if (stateMachine.getLastState() == SMTPState.STARTTLS) {
                    SslHandler sslHandler =  new SslHandler(engine, false);
                    ctx.getChannel().getPipeline().addFirst("sslHandler", sslHandler);
                    sslHandler.handshake();

                }
                */
                

                // We use a SMTPPipelinedRequest if the SMTPServer supports PIPELINING. This will allow the NETTY to get
                // the MAX throughput as the encoder will write it out in one buffer if possible. This result in less system calls
                if (supportsPipelining && config.getPipeliningMode() != PipeliningMode.NO) {
                    session.getAttributes().put(PIPELINING_ACTIVE_KEY, true);
                    session.send(SMTPRequestImpl.mail(mailFrom), new MailResponseCallback(future, statusList, recipients, msg, config));
                    for (int i = 0; i < recipients.size(); i++) {
                        String rcpt = recipients.get(i);                      
                        session.send(SMTPRequestImpl.rcpt(rcpt), new RcptResponseCallback(future, statusList, recipients, msg, rcpt, config));

                    }
                    session.send(SMTPRequestImpl.data(), new DataResponseCallback(future, statusList, msg));
                } else {
                    session.send(SMTPRequestImpl.mail(mailFrom), new MailResponseCallback(future, statusList, recipients, msg, config));
                }

            } else {
                while (!recipients.isEmpty()) {
                    statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                }

                future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                session.close();

            }
        }
        
        
        private Set<String> getSupportedExtensions(SMTPResponse response) {
            Set<String> extensions = new HashSet<String>();
            Iterator<String> lines = response.getLines().iterator();
            while(lines.hasNext()) {
                String line = lines.next();
                if (line.equalsIgnoreCase(PIPELINING_EXTENSION)) {
                    extensions.add(PIPELINING_EXTENSION);
                } else if (line.equalsIgnoreCase(STARTTLS_EXTENSION)) {
                    extensions.add(STARTTLS_EXTENSION);
                }
            }
            return extensions;
        }
        
    }
    
    private final class MailResponseCallback extends AbstractResponseCallback {
        private SMTPClientConfig config;
        private LinkedList<String> recipients;
        private List<DeliveryRecipientStatus> statusList;
        private MessageInput msg;
        
        public MailResponseCallback(SMTPClientFuture future, final List<DeliveryRecipientStatus> statusList, final LinkedList<String> recipients, final MessageInput msg,  final SMTPClientConfig config) {
            super(future);
            this.config = config;
            this.recipients = recipients;
            this.msg = msg;
            this.statusList = statusList;
        }
        

        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {

            int code = response.getCode();

            if (code > 400) {
                while (!recipients.isEmpty()) {
                    statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                }

                future.setDeliveryStatus(new DeliveryResultImpl(statusList));                
                session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                session.close();
            } else {
                String rcpt = recipients.removeFirst();
                
                // only write the request if the SMTPServer does not support PIPELINING and we don't want to use it
                // as otherwise we already sent this 
                if (!session.getAttributes().containsKey(PIPELINING_ACTIVE_KEY) || config.getPipeliningMode() == PipeliningMode.NO) {
                    session.send(SMTPRequestImpl.rcpt(rcpt), new RcptResponseCallback(future, statusList, recipients, msg, rcpt, config));
                }
            }
          
        }
        
    }
    
    private final class RcptResponseCallback extends AbstractResponseCallback {
        private SMTPClientConfig config;
        private LinkedList<String> recipients;
        private List<DeliveryRecipientStatus> statusList;
        private MessageInput msg;
        private String rcpt;

        public RcptResponseCallback(SMTPClientFuture future, final List<DeliveryRecipientStatus> statusList, final LinkedList<String> recipients, final MessageInput msg,  final String rcpt, final SMTPClientConfig config) {
            super(future);
            this.config = config;
            this.recipients = recipients;
            this.msg = msg;
            this.statusList = statusList;
            this.rcpt = rcpt;
        }
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            statusList.add(new DeliveryRecipientStatusImpl(rcpt, response));
            if (!recipients.isEmpty()) {
                String rcpt = recipients.removeFirst();

                // only write the request if the SMTPServer does not support
                // PIPELINING and we don't want to use it
                // as otherwise we already sent this
                if (!session.getAttributes().containsKey(PIPELINING_ACTIVE_KEY) || config.getPipeliningMode() == PipeliningMode.NO) {
                    session.send(SMTPRequestImpl.rcpt(rcpt), new RcptResponseCallback(future, statusList, recipients, msg, rcpt, config));
                }
            } else {

                boolean success = false;
                for (int i = 0; i < statusList.size(); i++) {
                    if (statusList.get(i).getStatus() == Status.Ok) {
                        success = true;
                        break;
                    }
                }
                if (success) {
                    // only write the request if the SMTPServer does not support
                    // PIPELINING and we don't want to use it
                    // as otherwise we already sent this
                    if (!session.getAttributes().containsKey(PIPELINING_ACTIVE_KEY) || config.getPipeliningMode() == PipeliningMode.NO) {
                        session.send(SMTPRequestImpl.data(), new DataResponseCallback(future, statusList, msg));
                    }

                } else {

                    // all recipients failed so we should now complete the
                    // future
                    future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                    
                    session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                    session.close();
                }
            }
        }

    }
    
    private final class DataResponseCallback extends AbstractResponseCallback {
        private List<DeliveryRecipientStatus> statusList;
        private MessageInput msg;

        public DataResponseCallback(SMTPClientFuture future, final List<DeliveryRecipientStatus> statusList, final MessageInput msg) {
            super(future);
            this.msg = msg;
            this.statusList = statusList;
        }
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            int code = response.getCode();

            if (code < 400) {
                session.send(msg, new PostDataResponseCallback(future, statusList));
            } else {
                Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                while(status.hasNext()) {
                    ((DeliveryRecipientStatusImpl)status.next()).setResponse(response);
                }
                future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                session.close();
            }            
        }
        
    }
    
    private final class PostDataResponseCallback extends AbstractResponseCallback {
        private List<DeliveryRecipientStatus> statusList;

        public PostDataResponseCallback(SMTPClientFuture future, final List<DeliveryRecipientStatus> statusList) {
            super(future);
            this.future = future;
            this.statusList = statusList;
        }
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            int code = response.getCode();

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

            } else {
                Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                while(status.hasNext()) {
                    ((DeliveryRecipientStatusImpl)status.next()).setResponse(response);
                }
                future.setDeliveryStatus(new DeliveryResultImpl(statusList));

            }    
            session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
            session.close();
        }

        
    }
    
    /**
     * Abstract base implementation of {@link SMTPResponseCallback} which comple the {@link SMTPClientFuture} on an {@link Exception}
     * 
     * @author Norman Maurer
     *
     */
    protected abstract class AbstractResponseCallback implements SMTPResponseCallback {

        protected SMTPClientFuture future;

        public AbstractResponseCallback(SMTPClientFuture future) {
            this.future = future;
        }
        
        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            future.setDeliveryStatus(DeliveryResultImpl.create(t));
            if (session != null) {
                session.close();
            }
        }
        
    }

}
