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

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

import org.apache.commons.codec.binary.Base64;

/**
 * {@link AbstractAuthResponseCallback} which handles <code>AUTH LOGIN</code>
 * 
 * @author Norman Maurer
 *
 */
public class AuthLoginResponseCallback extends AbstractAuthResponseCallback{


    /**
     * Get instance of this {@link SMTPResponseCallback} implementation
     */
    public final static SMTPResponseCallback INSTANCE = new AuthLoginResponseCallback();
    
    private final static String PROCESS_USERNAME = "PROCESS_USERNAME";
    private final static String PROCESS_PASSWORD = "PROCESS_PASSWORD";

    private AuthLoginResponseCallback() {
        
    }
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) {
        if (session.getAttributes().containsKey(PROCESS_PASSWORD)) {
            session.getAttributes().remove(PROCESS_PASSWORD);
            if (response.getCode() == 235) {
                String mail = (String) session.getAttributes().get(SENDER_KEY);
                
                boolean supportsPipelining = session.getSupportedExtensions().contains(PIPELINING_EXTENSION);
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
                setDeliveryStatusForAll(session, response);
            }
        } else if (session.getAttributes().containsKey(PROCESS_USERNAME)) {
            session.getAttributes().remove(PROCESS_USERNAME);

            if (response.getCode() == 334) {
                session.getAttributes().put(PROCESS_PASSWORD, true);
                session.send(new SMTPRequestImpl(Base64.encodeBase64String(session.getConfig().getAuthentication().getPassword().getBytes(CHARSET)), null), INSTANCE);
            } else {
                setDeliveryStatusForAll(session, response);

            }
        } else {
            if (response.getCode() == 334) {
                session.getAttributes().put(PROCESS_USERNAME, true);
                session.send(new SMTPRequestImpl(Base64.encodeBase64String(session.getConfig().getAuthentication().getUsername().getBytes(CHARSET)), null), INSTANCE);
            } else {
                setDeliveryStatusForAll(session, response);
            }
        }
    }

}
