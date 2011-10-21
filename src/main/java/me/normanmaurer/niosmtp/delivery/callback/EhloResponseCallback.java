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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import me.normanmaurer.niosmtp.Authentication;
import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;
import me.normanmaurer.niosmtp.SMTPClientConstants;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPUnsupportedExtensionException;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryTransaction;
import me.normanmaurer.niosmtp.transport.DeliveryMode;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link AbstractResponseCallback} implementation which will handle the <code>EHLO</code> {@link SMTPResponse}
 * 
 * It will write the next {@link SMTPRequest} to the {@link SMTPClientSession} with the right {@link SMTPResponseCallback} added.
 * 
 * This implementation also handles the <code>PIPELINING</code> and also the <code>STARTTLS</code> extension
 * 
 * @author Norman Maurer
 *
 */
public class EhloResponseCallback extends AbstractResponseCallback implements SMTPClientConstants{

    
    /**
     * Get instance of this {@link SMTPResponseCallback} implemenation
     */
    public static final SMTPResponseCallback INSTANCE = new EhloResponseCallback();
    
    private static final SMTPException PIPELINING_NOT_SUPPORTED_EXECTION = new SMTPUnsupportedExtensionException("Extension PIPELINING is not supported");
    
    private static final SMTPException STARTTLS_NOT_SUPPORTED_EXECTION = new SMTPUnsupportedExtensionException("Extension STARTTLS is not supported");

    private EhloResponseCallback() {
        
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

        String mail = ((SMTPDeliveryTransaction)session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY)).getSender();
        if (code < 400) {

            // Check if we depend on pipelining 
            if (!supportsPipelining && session.getConfig().getPipeliningMode() == PipeliningMode.DEPEND) {
                onException(session, PIPELINING_NOT_SUPPORTED_EXECTION);
                return;
            }

            if (!supportsStartTLS && session.getDeliveryMode() == DeliveryMode.STARTTLS_DEPEND) {
                onException(session, STARTTLS_NOT_SUPPORTED_EXECTION);
                return;
            }
            
            
            
            if (supportsStartTLS && (session.getDeliveryMode() == DeliveryMode.STARTTLS_DEPEND || session.getDeliveryMode() == DeliveryMode.STARTTLS_TRY)) {
                session.send(SMTPRequestImpl.startTls(), StartTlsResponseCallback.INSTANCE);
            } else {
                Authentication auth = session.getConfig().getAuthentication();
                if (auth == null) {
                    // We use a SMTPPipelinedRequest if the SMTPServer supports
                    // PIPELINING. This will allow the NETTY to get
                    // the MAX throughput as the encoder will write it out in one
                    // buffer if possible. This result in less system calls
                    if (supportsPipelining && session.getConfig().getPipeliningMode() != PipeliningMode.NO) {
                        pipelining(session);
                    } else {
                        session.send(SMTPRequestImpl.mail(mail), MailResponseCallback.INSTANCE);
                    }
                } else {
                    switch (auth.getMode()) {
                    case Plain:
                        session.send(SMTPRequestImpl.authPlain(), AuthPlainResponseCallback.INSTANCE);

                        break;
                    case Login:
                        session.send(SMTPRequestImpl.authLogin(), AuthLoginResponseCallback.INSTANCE);

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
