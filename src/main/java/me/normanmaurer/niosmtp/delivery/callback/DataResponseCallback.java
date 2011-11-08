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

import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.delivery.impl.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link ChainedSMTPClientFutureListener} implementation which will handle the <code>DATA</code> {@link SMTPResponse}
 * 
 * It will write the next {@link SMTPRequest} to the {@link SMTPClientSession} with the right {@link SMTPClientFutureListener} added.
 * 
 * 
 * @author Norman Maurer
 *
 */
public class DataResponseCallback extends AbstractPipeliningResponseListener {

    /**
     * Get instance of this {@link SMTPResponseCallback} implementation
     */
    public final static DataResponseCallback INSTANCE = new DataResponseCallback();
    
    protected DataResponseCallback() {
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onResponseInternal(SMTPClientSession session, SMTPResponse response) throws SMTPException {

        SMTPClientFutureImpl<?> future = (SMTPClientFutureImpl<?>) session.getAttributes().get(FUTURE_KEY);
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttributes().get(DELIVERY_STATUS_KEY);
        SMTPMessage msg = ((SMTPDeliveryEnvelope) session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY)).getMessage();
        boolean pipeliningActive = session.getAttributes().containsKey(PIPELINING_ACTIVE_KEY);

        int code = response.getCode();

        if (code < 400) {
            next(session, msg);
        } else {
            if (!pipeliningActive || !future.isDone()) {
                Iterator<DeliveryRecipientStatus> status = statusList.iterator();
                while(status.hasNext()) {
                    ((DeliveryRecipientStatusImpl)status.next()).setResponse(response);
                }
                setDeliveryStatus(session);
            } 

        }            
    }
    
}
