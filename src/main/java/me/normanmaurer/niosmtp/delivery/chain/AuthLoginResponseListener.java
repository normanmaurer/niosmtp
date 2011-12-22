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
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig.PipeliningMode;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

import org.apache.commons.codec.binary.Base64;

/**
 * {@link AbstractAuthResponseListener} which handles <code>AUTH LOGIN</code>
 * 
 * @author Norman Maurer
 *
 */
public class AuthLoginResponseListener extends AbstractAuthResponseListener{


    /**
     * Get instance of this {@link AuthLoginResponseListener} implementation
     */
    public final static AuthLoginResponseListener INSTANCE = new AuthLoginResponseListener();
    
    private final static String PROCESS_USERNAME = "PROCESS_USERNAME";
    private final static String PROCESS_PASSWORD = "PROCESS_PASSWORD";

    private AuthLoginResponseListener() {
        
    }
    @Override
    public void onResult(SMTPClientSession session, SMTPResponse response) throws SMTPException {
        if (session.getAttribute(PROCESS_PASSWORD) != null) {
            session.setAttribute(PROCESS_PASSWORD, null);
            if (response.getCode() == 235) {
                String mail = ((SMTPDeliveryEnvelope)session.getAttribute(CURRENT_SMTP_TRANSACTION_KEY)).getSender();
                
                boolean supportsPipelining = session.getSupportedExtensions().contains(PIPELINING_EXTENSION);
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
                setDeliveryStatusForAll(session, response);
            }
        } else if (session.getAttribute(PROCESS_USERNAME) != null) {
            session.setAttribute(PROCESS_USERNAME, null);

            if (response.getCode() == 334) {
                session.setAttribute(PROCESS_PASSWORD, true);
                SMTPClientFuture<FutureResult<SMTPResponse>> future = session.send(new SMTPRequestImpl(new String(Base64.encodeBase64(((SMTPDeliveryAgentConfig)session.getConfig()).getAuthentication().getPassword().getBytes(CHARSET)), CHARSET), null));
                future.addListener(INSTANCE);
            } else {
                setDeliveryStatusForAll(session, response);

            }
        } else {
            if (response.getCode() == 334) {
                session.setAttribute(PROCESS_USERNAME, true);
                SMTPClientFuture<FutureResult<SMTPResponse>> future = session.send(new SMTPRequestImpl(new String(Base64.encodeBase64(((SMTPDeliveryAgentConfig)session.getConfig()).getAuthentication().getUsername().getBytes(CHARSET)), CHARSET), null));
                future.addListener(INSTANCE);
            } else {
                setDeliveryStatusForAll(session, response);
            }
        }
    }

}
