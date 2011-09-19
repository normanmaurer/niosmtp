/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* niosmtp licenses this file to You under the Apache License, Version 2.0
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
package me.normanmaurer.niosmtp.client.callback;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientConstants;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPUnsupportedExtensionException;
import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.client.DeliveryResultImpl;
import me.normanmaurer.niosmtp.client.SMTPClientFuture;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

public class EhloResponseCallback extends AbstractResponseCallback implements ResponseCallbackConstants, SMTPClientConstants{
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
