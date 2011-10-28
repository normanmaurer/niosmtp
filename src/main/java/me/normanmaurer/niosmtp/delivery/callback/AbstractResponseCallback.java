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
import java.util.Map;

import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.delivery.DeliveryResult;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig.PipeliningMode;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryFuture;
import me.normanmaurer.niosmtp.delivery.SMTPDeliverySessionConstants;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.delivery.impl.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.delivery.impl.DeliveryResultImpl;
import me.normanmaurer.niosmtp.delivery.impl.SMTPDeliveryFutureImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Abstract base implementation of {@link SMTPResponseCallback} which comple the {@link SMTPDeliveryFuture} on an {@link Exception}
 * 
 * @author Norman Maurer
 *
 */
public abstract class AbstractResponseCallback implements SMTPResponseCallback, SMTPDeliverySessionConstants, SMTPClientConstants {
    
    @SuppressWarnings("unchecked")
    @Override
    public void onException(SMTPClientSession session, Throwable t) {
        SMTPDeliveryFutureImpl future = (SMTPDeliveryFutureImpl) session.getAttributes().get(FUTURE_KEY);
        
        List<DeliveryResult> resultList = ((List<DeliveryResult>) session.getAttributes().get(DELIVERY_RESULT_LIST_KEY));
        Iterator<SMTPDeliveryEnvelope> transactions = ((Iterator<SMTPDeliveryEnvelope>) session.getAttributes().get(SMTP_TRANSACTIONS_KEY));
        
        resultList.add(DeliveryResultImpl.create(t));
        while(transactions.hasNext()) {
            // Remove the transactions from iterator and place a DeliveryResult which contains a Exception
            transactions.next();
            resultList.add(DeliveryResultImpl.create(t));
        }
        
        future.setDeliveryStatus(resultList);
        try {
            next(session, SMTPRequestImpl.quit());
        } catch (SMTPException e) {
            // ignore on close
        }

        session.close();
    }
    
    @SuppressWarnings("unchecked")
    protected void setDeliveryStatusForAll(SMTPClientSession session, SMTPResponse response) throws SMTPException {
        Iterator<String> recipients = (Iterator<String>) session.getAttributes().get(RECIPIENTS_KEY);
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttributes().get(DELIVERY_STATUS_KEY);

        while (recipients.hasNext()) {
            statusList.add(new DeliveryRecipientStatusImpl(recipients.next(), response));
        }

        setDeliveryStatus(session);
    }
    
    /**
     * Init the SMTPClienSesion by adding all needed data to the attributes
     * 
     * @param session
     */
    @SuppressWarnings("unchecked")
    private void initSession(SMTPClientSession session) {
        Map<String, Object> attrs = session.getAttributes();
        Iterator<SMTPDeliveryEnvelope> transactionList = ((Iterator<SMTPDeliveryEnvelope>) session.getAttributes().get(SMTP_TRANSACTIONS_KEY));
        SMTPDeliveryEnvelope transaction =  transactionList.next();
        
        attrs.put(CURRENT_SMTP_TRANSACTION_KEY,transaction);
        attrs.put(RECIPIENTS_KEY, transaction.getRecipients().iterator());
        attrs.put(DELIVERY_STATUS_KEY, new ArrayList<DeliveryRecipientStatus>());

        // cleanup old attribute
        attrs.remove(CURRENT_RCPT_KEY);

    }
    
    
    
    /**
     * Use the SMTP <code>PIPELINING</code> extension to send the commands to the remote SMTP Server
     * 
     * @param session
     * @throws SMTPException 
     */
    protected void pipelining(SMTPClientSession session) throws SMTPException {
        SMTPDeliveryEnvelope transaction = (SMTPDeliveryEnvelope) session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY);
        
        session.getAttributes().put(PIPELINING_ACTIVE_KEY, true);
        next(session, SMTPRequestImpl.mail(transaction.getSender()));
        for (String rcpt: transaction.getRecipients()) {
            next(session, SMTPRequestImpl.rcpt(rcpt));
        }
        next(session, SMTPRequestImpl.data());
    }
    
    
    /**
     * Set the DeliveryStatus and notify the {@link SMTPDeliveryFuture} if needed
     * 
     * @param session
     * @throws SMTPException 
     */
    @SuppressWarnings("unchecked")
    protected void setDeliveryStatus(SMTPClientSession session) throws SMTPException {
        SMTPDeliveryFutureImpl future = (SMTPDeliveryFutureImpl) session.getAttributes().get(FUTURE_KEY);
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttributes().get(DELIVERY_STATUS_KEY);
        List<DeliveryResult> resultList = ((List<DeliveryResult>) session.getAttributes().get(DELIVERY_RESULT_LIST_KEY));       
        Iterator<SMTPDeliveryEnvelope> transactions = ((Iterator<SMTPDeliveryEnvelope>) session.getAttributes().get(SMTP_TRANSACTIONS_KEY));

        resultList.add(new DeliveryResultImpl(statusList));
        
        if (!transactions.hasNext()) {
            future.setDeliveryStatus(resultList);

            next(session, SMTPRequestImpl.quit());
            session.close();


        } else {
            initSession(session);
            if (session.getSupportedExtensions().contains(PIPELINING_EXTENSION) && ((SMTPDeliveryAgentConfig)session.getConfig()).getPipeliningMode() != PipeliningMode.NO) {
                pipelining(session);
            } else {
                String sender = ((SMTPDeliveryEnvelope) session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY)).getSender();

                next(session, SMTPRequestImpl.mail(sender));
            }
        }

    }
    
    protected final void next(SMTPClientSession session, SMTPRequest request) throws SMTPException {
        SMTPResponseCallback callback = ((SMTPResponseCallbackFactory) session.getAttributes().get(SMTP_RESPONSE_CALLBACK_FACTORY)).getCallback(session, request);
        session.send(request, callback);
    }
    
    protected final void next(SMTPClientSession session, SMTPMessage request) throws SMTPException {
        SMTPResponseCallback callback = ((SMTPResponseCallbackFactory) session.getAttributes().get(SMTP_RESPONSE_CALLBACK_FACTORY)).getCallback(session, request);
        session.send(request, callback);
    }
}