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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus.DeliveryStatus;
import me.normanmaurer.niosmtp.delivery.impl.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link ChainedSMTPClientFutureListener} which handles the {@link SMTPResponse} for the DATA finish sequence in the LMTP protocol
 * 
 * @author Norman Maurer
 *
 */
public class LMTPPostDataResponseCallback extends ChainedSMTPClientFutureListener<Collection<SMTPResponse>>{
    
    
    /**
     * {@link LMTPPostDataResponseCallback} instance to use
     */
    public final static LMTPPostDataResponseCallback INSTANCE = new LMTPPostDataResponseCallback();

    
    private LMTPPostDataResponseCallback() {
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onResult(SMTPClientSession session, Collection<SMTPResponse> response) throws SMTPException {
        
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttribute(DELIVERY_STATUS_KEY);

        
        Iterator<SMTPResponse> responses = response.iterator();
        Iterator<DeliveryRecipientStatus> status = statusList.iterator();
        while(status.hasNext()) {
            DeliveryRecipientStatus s = status.next();
            if (s.getStatus() == DeliveryStatus.Ok) {
                ((DeliveryRecipientStatusImpl)s).setResponse(responses.next());
            }
        }            
        setDeliveryStatus(session);        
    }
}
