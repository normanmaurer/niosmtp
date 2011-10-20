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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.normanmaurer.niosmtp.SMTPClientConstants;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.client.DeliveryResult;
import me.normanmaurer.niosmtp.client.DeliveryResultImpl;
import me.normanmaurer.niosmtp.client.SMTPClientFuture;
import me.normanmaurer.niosmtp.client.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.client.SMTPClientSessionConstants;
import me.normanmaurer.niosmtp.client.SMTPTransaction;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Abstract base implementation of {@link SMTPResponseCallback} which comple the {@link SMTPClientFuture} on an {@link Exception}
 * 
 * @author Norman Maurer
 *
 */
public abstract class AbstractResponseCallback implements SMTPResponseCallback, SMTPClientSessionConstants, SMTPClientConstants {
    
    @SuppressWarnings("unchecked")
    @Override
    public void onException(SMTPClientSession session, Throwable t) {
        SMTPClientFutureImpl future = (SMTPClientFutureImpl) session.getAttributes().get(FUTURE_KEY);
        
        List<DeliveryResult> resultList = ((List<DeliveryResult>) session.getAttributes().get(DELIVERY_RESULT_LIST_KEY));
        Iterator<SMTPTransaction> transactions = ((Iterator<SMTPTransaction>) session.getAttributes().get(SMTP_TRANSACTIONS_KEY));
        
        resultList.add(DeliveryResultImpl.create(t));
        while(transactions.hasNext()) {
            // Remove the transactions from iterator and place a DeliveryResult which contains a Exception
            transactions.next();
            resultList.add(DeliveryResultImpl.create(t));
        }
        
        future.setDeliveryStatus(resultList);
        session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
        session.close();
    }
    
    @SuppressWarnings("unchecked")
    protected void setDeliveryStatusForAll(SMTPClientSession session, SMTPResponse response) {
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
        Iterator<SMTPTransaction> transactionList = ((Iterator<SMTPTransaction>) session.getAttributes().get(SMTP_TRANSACTIONS_KEY));
        SMTPTransaction transaction =  transactionList.next();
        
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
     */
    protected void pipelining(SMTPClientSession session) {
        SMTPTransaction transaction = (SMTPTransaction) session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY);
        
        session.getAttributes().put(PIPELINING_ACTIVE_KEY, true);
        session.send(SMTPRequestImpl.mail(transaction.getSender()), MailResponseCallback.INSTANCE);
        for (String rcpt: transaction.getRecipients()) {
            session.send(SMTPRequestImpl.rcpt(rcpt), RcptResponseCallback.INSTANCE);
        }
        session.send(SMTPRequestImpl.data(), DataResponseCallback.INSTANCE);
    }
    
    
    /**
     * Set the DeliveryStatus and notify the {@link SMTPClientFuture} if needed
     * 
     * @param session
     */
    @SuppressWarnings("unchecked")
    protected void setDeliveryStatus(SMTPClientSession session) {
        SMTPClientFutureImpl future = (SMTPClientFutureImpl) session.getAttributes().get(FUTURE_KEY);
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttributes().get(DELIVERY_STATUS_KEY);
        List<DeliveryResult> resultList = ((List<DeliveryResult>) session.getAttributes().get(DELIVERY_RESULT_LIST_KEY));       
        Iterator<SMTPTransaction> transactions = ((Iterator<SMTPTransaction>) session.getAttributes().get(SMTP_TRANSACTIONS_KEY));

        resultList.add(new DeliveryResultImpl(statusList));
        
        if (!transactions.hasNext()) {
            future.setDeliveryStatus(resultList);

            session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
            session.close();

        } else {
            initSession(session);
            if (session.getSupportedExtensions().contains(PIPELINING_EXTENSION) && session.getConfig().getPipeliningMode() != PipeliningMode.NO) {
                pipelining(session);
            } else {
                String sender = ((SMTPTransaction) session.getAttributes().get(CURRENT_SMTP_TRANSACTION_KEY)).getSender();

                session.send(SMTPRequestImpl.mail(sender), MailResponseCallback.INSTANCE);
            }
        }

    }
}