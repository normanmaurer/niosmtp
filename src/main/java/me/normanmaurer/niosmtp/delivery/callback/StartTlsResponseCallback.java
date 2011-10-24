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

import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig.PipeliningMode;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryTransaction;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link AbstractResponseCallback} implementation which will handle the <code>STARTLS</code> {@link SMTPResponse}
 * 
 * It will write the next {@link SMTPRequest} to the {@link SMTPClientSession} with the right {@link SMTPResponseCallback} added.
 * 
 * This implementation also handles the <code>PIPELINING</code> and also the <code>STARTTLS</code> extension
 * 
 * @author Norman Maurer
 *
 */
public class StartTlsResponseCallback extends AbstractResponseCallback implements SMTPClientConstants {

    /**
     * Get instance of this {@link SMTPResponseCallback} implemenation
     */
    public final static SMTPResponseCallback INSTANCE = new StartTlsResponseCallback();

    private StartTlsResponseCallback() {
        
    }
    
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) {
        String mail = ((SMTPDeliveryTransaction) session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY)).getSender();

        
        int code = response.getCode();
        if (code < 400) {
            
            session.startTLS();
           

            // We use a SMTPPipelinedRequest if the SMTPServer supports PIPELINING. This will allow the NETTY to get
            // the MAX throughput as the encoder will write it out in one buffer if possible. This result in less system calls
            if (session.getSupportedExtensions().contains(PIPELINING_EXTENSION) && ((SMTPDeliveryAgentConfig)session.getConfig()).getPipeliningMode() != PipeliningMode.NO) {
                pipelining(session);
            } else {
                session.send(SMTPRequestImpl.mail(mail), MailResponseCallback.INSTANCE);
            }

        } else {
            setDeliveryStatusForAll(session, response);
        }
    }

}
