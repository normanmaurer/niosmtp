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
package me.normanmaurer.niosmtp.delivery.chain;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig.PipeliningMode;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link ChainedSMTPClientFutureListener} implementation which will handle the <code>STARTLS</code> {@link SMTPResponse}
 * 
 * It will write the next {@link SMTPRequest} to the {@link SMTPClientSession} with the right {@link SMTPResponseCallback} added.
 * 
 * This implementation also handles the <code>PIPELINING</code> and also the <code>STARTTLS</code> extension
 * 
 * @author Norman Maurer
 *
 */
public class StartTlsResponseListener extends ChainedSMTPClientFutureListener<SMTPResponse> implements SMTPClientConstants {

    /**
     * Get instance of this {@link StartTlsResponseListener} implementation
     */
    public final static StartTlsResponseListener INSTANCE = new StartTlsResponseListener();

    private StartTlsResponseListener() {
        
    }
    
    @Override
    public void onResult(final SMTPClientSession session, SMTPResponse response) throws SMTPException {
        
        int code = response.getCode();
        if (code < 400) {
            
            session.startTLS().addListener(new SMTPClientFutureListener<FutureResult<Boolean>>() {
                
                @Override
                public void operationComplete(SMTPClientFuture<FutureResult<Boolean>> future) {
                    try {
                        // We use a SMTPPipelinedRequest if the SMTPServer supports PIPELINING. This will allow the NETTY to get
                        // the MAX throughput as the encoder will write it out in one buffer if possible. This result in less system calls
                        if (session.getSupportedExtensions().contains(PIPELINING_EXTENSION) && ((SMTPDeliveryAgentConfig)session.getConfig()).getPipeliningMode() != PipeliningMode.NO) {
                            pipelining(session);
                        } else {
                            String mail = ((SMTPDeliveryEnvelope) session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY)).getSender();

                            next(session, SMTPRequestImpl.mail(mail));
                        }
                    } catch (SMTPException e) {
                        onException(session, e);
                    }
                    
                }
            });
           

           
        } else {
            setDeliveryStatusForAll(session, response);
        }
    }

}
