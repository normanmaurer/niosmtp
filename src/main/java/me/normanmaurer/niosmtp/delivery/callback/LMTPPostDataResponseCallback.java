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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.MultiResponseCallback;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus.DeliveryStatus;
import me.normanmaurer.niosmtp.delivery.impl.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link AbstractResponseCallback} which handles the {@link SMTPResponse} for the DATA finish sequence in the LMTP protocol
 * 
 * @author Norman Maurer
 *
 */
public class LMTPPostDataResponseCallback extends AbstractResponseCallback implements MultiResponseCallback{
    
    
    /**
     * {@link LMTPPostDataResponseCallback} instance to use
     */
    public final static SMTPResponseCallback INSTANCE = new LMTPPostDataResponseCallback();

    private final static String DATA_PROCESSING = "DATA_PROCESSING";
    private final static String SUCCESSFUL_RECPIENTS = "SUCCESSFUL_RECIPIENTS";
    
    private LMTPPostDataResponseCallback() {
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) throws SMTPException {
        
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttributes().get(DELIVERY_STATUS_KEY);
        if (!session.getAttributes().containsKey(DATA_PROCESSING)) {
            session.getAttributes().put(DATA_PROCESSING, true);
            List<DeliveryRecipientStatusImpl> successful = new ArrayList<DeliveryRecipientStatusImpl>();
            Iterator<DeliveryRecipientStatus> status = statusList.iterator();
            while(status.hasNext()) {
                DeliveryRecipientStatus s = status.next();
                if (s.getStatus() == DeliveryStatus.Ok) {
                    successful.add((DeliveryRecipientStatusImpl)s);
                }
            }
            session.getAttributes().put(SUCCESSFUL_RECPIENTS, successful.iterator());
            
        }
        DeliveryRecipientStatusImpl status = ((Iterator<DeliveryRecipientStatusImpl>)session.getAttributes().get(SUCCESSFUL_RECPIENTS)).next();
        status.setResponse(response);
        
        if (isDone(session)) {
            session.getAttributes().remove(SUCCESSFUL_RECPIENTS);
            session.getAttributes().remove(DATA_PROCESSING);
            setDeliveryStatus(session);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isDone(SMTPClientSession session) {
        Iterator<DeliveryRecipientStatusImpl> statusIt = (Iterator<DeliveryRecipientStatusImpl>)session.getAttributes().get(SUCCESSFUL_RECPIENTS);
        return (statusIt == null || !statusIt.hasNext());
    }

}
