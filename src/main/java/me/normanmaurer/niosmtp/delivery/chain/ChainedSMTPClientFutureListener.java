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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPPipeliningRequest;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.core.SMTPMessageSubmitImpl;
import me.normanmaurer.niosmtp.core.SMTPPipeliningRequestImpl;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus.DeliveryStatus;
import me.normanmaurer.niosmtp.delivery.FutureResult;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig.PipeliningMode;
import me.normanmaurer.niosmtp.delivery.SMTPDeliverySessionConstants;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.delivery.impl.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.delivery.impl.DeliveryResultImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Abstract base implementation of {@link SMTPResponseCallback} which comple the {@link SMTPClientFuture} on an {@link Exception}
 * 
 * @author Norman Maurer
 *
 */
public abstract class ChainedSMTPClientFutureListener<E> implements SMTPClientFutureListener<FutureResult<E>>, SMTPDeliverySessionConstants, SMTPClientConstants {
    
    @Override
    public void operationComplete(SMTPClientFuture<FutureResult<E>> future) {
        FutureResult<E> result = future.getNoWait();
        SMTPClientSession session = future.getSession();
        if (!result.isSuccess()) {
            onException(session, result.getException());
        } else {
            try {
                onResult(session, result.getResult());
            } catch (SMTPException e) {
                onException(session, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void onException(SMTPClientSession session, SMTPException e) {
        SMTPClientFutureImpl<Collection<FutureResult<Iterator<DeliveryRecipientStatus>>>> future = (SMTPClientFutureImpl<Collection<FutureResult<Iterator<DeliveryRecipientStatus>>>>) session.getAttributes().get(FUTURE_KEY);
        
        List<FutureResult<Iterator<DeliveryRecipientStatus>>> resultList = ((List<FutureResult<Iterator<DeliveryRecipientStatus>>>) session.getAttributes().get(DELIVERY_RESULT_LIST_KEY));
        Iterator<SMTPDeliveryEnvelope> transactions = ((Iterator<SMTPDeliveryEnvelope>) session.getAttributes().get(SMTP_TRANSACTIONS_KEY));
        
        resultList.add(DeliveryResultImpl.create(e));
        while(transactions.hasNext()) {
            // Remove the transactions from iterator and place a DeliveryResult which contains a Exception
            transactions.next();
            resultList.add(DeliveryResultImpl.create(e));
        }
        
        future.setDeliveryStatus(resultList);
        try {
            next(session, SMTPRequestImpl.quit());
        } catch (SMTPException e1) {
            // ignore on close
        }

        session.close();
    }
    
    protected abstract void onResult(SMTPClientSession session, E result) throws SMTPException;
    
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
        SMTPPipeliningRequest request = new SMTPPipeliningRequestImpl(transaction.getSender(), transaction.getRecipients().iterator());
        next(session, request);
        
    }
    
    
    /**
     * Set the DeliveryStatus and notify the {@link SMTPClientFuture} if needed
     * 
     * @param session
     * @throws SMTPException 
     */
    @SuppressWarnings("unchecked")
    protected void setDeliveryStatus(SMTPClientSession session) throws SMTPException {
        SMTPClientFutureImpl<Collection<FutureResult<Iterator<DeliveryRecipientStatus>>>> future = (SMTPClientFutureImpl<Collection<FutureResult<Iterator<DeliveryRecipientStatus>>>>) session.getAttributes().get(FUTURE_KEY);
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttributes().get(DELIVERY_STATUS_KEY);
        List<FutureResult<Iterator<DeliveryRecipientStatus>>> resultList = ((List<FutureResult<Iterator<DeliveryRecipientStatus>>>) session.getAttributes().get(DELIVERY_RESULT_LIST_KEY));
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
    
    protected final void next(SMTPClientSession session, SMTPPipeliningRequest request) throws SMTPException {
        SMTPClientFutureListenerFactory factory = (SMTPClientFutureListenerFactory) session.getAttributes().get(SMTP_RESPONSE_CALLBACK_FACTORY);
        session.send(request).addListener(factory.getListener(session, request));
    }
    
    protected final void next(SMTPClientSession session, SMTPRequest request) throws SMTPException {
        SMTPClientFutureListenerFactory factory = (SMTPClientFutureListenerFactory) session.getAttributes().get(SMTP_RESPONSE_CALLBACK_FACTORY);
        session.send(request).addListener(factory.getListener(session, request));
    }
    
    @SuppressWarnings("unchecked")
    protected final void next(SMTPClientSession session, SMTPMessage request) throws SMTPException {
        SMTPClientFutureListenerFactory factory = (SMTPClientFutureListenerFactory) session.getAttributes().get(SMTP_RESPONSE_CALLBACK_FACTORY);
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttributes().get(DELIVERY_STATUS_KEY);
        int rcpts = 0;
        for(DeliveryRecipientStatus status: statusList) {
            if(status.getStatus() == DeliveryStatus.Ok) {
                rcpts++;
            }
        }
        session.send(new SMTPMessageSubmitImpl(request, rcpts)).addListener(factory.getListener(session, request));
    }
}