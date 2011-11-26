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

import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;


/**
 * {@link ChainedSMTPClientFutureListener} implementation which will handle the <code>WELCOME</code> {@link SMTPResponse} which is triggered
 * after the connection is established to the SMTP Server
 * 
 * It will write the next {@link SMTPRequest} to the {@link SMTPClientSession} with the right {@link SMTPClientFutureListener} added.
 * 
 * 
 * @author Norman Maurer
 *
 */
public class WelcomeResponseListener extends ChainedSMTPClientFutureListener<SMTPResponse> {

    /**
     * Get instance of this {@link WelcomeResponseListener} implementation
     */
    public final static WelcomeResponseListener INSTANCE = new WelcomeResponseListener();
    
    private WelcomeResponseListener() {
        
    }
    
    @Override
    public void onResult(SMTPClientSession session, SMTPResponse response) throws SMTPException {
        int code = response.getCode();
        if (code < 400) {   
            next(session, SMTPRequestImpl.ehlo(((SMTPDeliveryAgentConfig)session.getConfig()).getHeloName()));
        } else {
            setDeliveryStatusForAll(session, response);
        }
    }
    
}
