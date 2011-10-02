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

import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;


/**
 * {@link AbstractResponseCallback} implementation which will handle the <code>WELCOME</code> {@link SMTPResponse} which is triggered
 * after the connection is established to the SMTP Server
 * 
 * It will write the next {@link SMTPRequest} to the {@link SMTPClientSession} with the right {@link SMTPResponseCallback} added.
 * 
 * 
 * @author Norman Maurer
 *
 */
public class WelcomeResponseCallback extends AbstractResponseCallback {

    /**
     * Get instance of this {@link SMTPResponseCallback} implemenation
     */
    public final static WelcomeResponseCallback INSTANCE = new WelcomeResponseCallback();
    
    private WelcomeResponseCallback() {
        
    }
    
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) {
        int code = response.getCode();
        if (code < 400) {            
            session.send(SMTPRequestImpl.ehlo(session.getConfig().getHeloName()), EhloResponseCallback.INSTANCE);
        } else {
            setDeliveryStatusForAll(session, response);

        }            
    }
    
}
