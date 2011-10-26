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

import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.LMTPRequest;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link AbstractResponseCallback} which will be used after the welcome {@link SMTPResponse} was sent from the LMTP Server
 * 
 * @author Norman Maurer
 *
 */
public class LMTPWelcomeResponseCallback extends AbstractResponseCallback {

    /**
     * Get instance of this {@link SMTPResponseCallback} implementation
     */
    public final static LMTPWelcomeResponseCallback INSTANCE = new LMTPWelcomeResponseCallback();
    
    private LMTPWelcomeResponseCallback() {
        
    }
    
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) throws SMTPException {
        int code = response.getCode();
        if (code < 400) {          
            next(session, LMTPRequest.lhlo(((SMTPDeliveryAgentConfig) session.getConfig()).getHeloName()));
        } else {
            setDeliveryStatusForAll(session, response);
        }            
    }
    

}
