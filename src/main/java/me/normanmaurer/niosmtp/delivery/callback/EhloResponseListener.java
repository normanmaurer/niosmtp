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
package me.normanmaurer.niosmtp.delivery.callback;

import java.util.Iterator;
import java.util.Set;

import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPUnsupportedExtensionException;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.Authentication;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig.PipeliningMode;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link ChainedSMTPClientFutureListener} implementation which will handle the <code>EHLO</code> {@link SMTPResponse}
 * 
 * It will write the next {@link SMTPRequest} to the {@link SMTPClientSession} with the right {@link SMTPResponseCallback} added.
 * 
 * This implementation also handles the <code>PIPELINING</code> and also the <code>STARTTLS</code> extension
 * 
 * @author Norman Maurer
 *
 */
public class EhloResponseListener extends ChainedSMTPClientFutureListener<SMTPResponse> implements SMTPClientConstants{

    
    /**
     * Get instance of this {@link SMTPResponseCallback} implemenation
     */
    public static final EhloResponseListener INSTANCE = new EhloResponseListener();
    
    private static final SMTPException PIPELINING_NOT_SUPPORTED_EXECTION = new SMTPUnsupportedExtensionException("Extension PIPELINING is not supported");
    
    private static final SMTPException STARTTLS_NOT_SUPPORTED_EXECTION = new SMTPUnsupportedExtensionException("Extension STARTTLS is not supported");

    private EhloResponseListener() {
        
    }
    
    @Override
    public void onResult(SMTPClientSession session, SMTPResponse response) throws SMTPException {
        boolean supportsPipelining = false;
        boolean supportsStartTLS = false;
        initSupportedExtensions(session, response);
        
        // Check if the SMTPServer supports PIPELINING 
        Set<String> extensions = session.getSupportedExtensions();

        if (extensions.contains(PIPELINING_EXTENSION)) {
            supportsPipelining = true;
        }
        if (extensions.contains(STARTTLS_EXTENSION)) {
            supportsStartTLS = true;
        }

        int code = response.getCode();

        String mail = ((SMTPDeliveryEnvelope)session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY)).getSender();
        if (code < 400) {

            // Check if we depend on pipelining 
            if (!supportsPipelining && ((SMTPDeliveryAgentConfig)session.getConfig()).getPipeliningMode() == PipeliningMode.DEPEND) {
                throw  PIPELINING_NOT_SUPPORTED_EXECTION;
            }

            if (!supportsStartTLS && session.getDeliveryMode() == SMTPDeliveryMode.STARTTLS_DEPEND) {
                throw STARTTLS_NOT_SUPPORTED_EXECTION;
            }
            
            
            
            if (supportsStartTLS && (session.getDeliveryMode() == SMTPDeliveryMode.STARTTLS_DEPEND || session.getDeliveryMode() == SMTPDeliveryMode.STARTTLS_TRY)) {
                next(session, SMTPRequestImpl.startTls());
            } else {
                Authentication auth = ((SMTPDeliveryAgentConfig)session.getConfig()).getAuthentication();
                if (auth == null) {
                    // We use a SMTPPipelinedRequest if the SMTPServer supports
                    // PIPELINING. This will allow the NETTY to get
                    // the MAX throughput as the encoder will write it out in one
                    // buffer if possible. This result in less system calls
                    if (supportsPipelining && ((SMTPDeliveryAgentConfig)session.getConfig()).getPipeliningMode() != PipeliningMode.NO) {
                        pipelining(session);
                    } else {
                        next(session, SMTPRequestImpl.mail(mail));
                    }
                } else {
                    switch (auth.getMode()) {
                    case Plain:
                        next(session, SMTPRequestImpl.authPlain());

                        break;
                    case Login:
                        next(session, SMTPRequestImpl.authLogin());
                    default:
                        break;
                    }
                }
            }

        } else {
            setDeliveryStatusForAll(session, response);

        }
        
    }
    
    
    
    /**
     * Return all supported extensions which are included in the {@link SMTPResponse}
     * 
     * @param response
     * @return extensions
     */
    private void initSupportedExtensions(SMTPClientSession session, SMTPResponse response) {
        Iterator<String> lines = response.getLines().iterator();
        while(lines.hasNext()) {
            String line = lines.next();
            if (line.equalsIgnoreCase(PIPELINING_EXTENSION)) {
                session.addSupportedExtensions(PIPELINING_EXTENSION);
            } else if (line.equalsIgnoreCase(STARTTLS_EXTENSION)) {
                session.addSupportedExtensions(STARTTLS_EXTENSION);
            }
        }
    }
    
}
