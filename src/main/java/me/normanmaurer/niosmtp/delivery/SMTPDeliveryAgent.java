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
package me.normanmaurer.niosmtp.delivery;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.ArrayIterator;
import me.normanmaurer.niosmtp.delivery.callback.WelcomeResponseCallback;
import me.normanmaurer.niosmtp.delivery.impl.SMTPDeliveryFutureImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;



/**
 * {@link SMTPDeliveryAgent} which use the wrapped {@link SMTPClientTransport} to deliver email
 * 
 * 
 * @author Norman Maurer
 * 
 */
public class SMTPDeliveryAgent implements SMTPClientConstants, SMTPDeliverySessionConstants {

    private final SMTPClientTransport transport;

    public SMTPDeliveryAgent(final SMTPClientTransport transport) {
        this.transport = transport;
    }

    
    


    /**
     * Deliver the given {@link SMTPDeliveryTransaction}'s 
     * 
     * The implementation may choose to do the deliver in an async fashion. 
     * 
     * @param host
     * @param config
     * @param transation
     * @return future
     */
    public SMTPDeliveryFuture deliver(InetSocketAddress host, final SMTPDeliveryAgentConfig config, final SMTPDeliveryTransaction... transactions) {
        if (transactions == null || transactions.length == 0) {
            throw new IllegalArgumentException("SMTPTransaction parameter must be not null and the length must be > 0");
        }

        final SMTPDeliveryFutureImpl future = new SMTPDeliveryFutureImpl();

        
        transport.connect(host, config,new SMTPResponseCallback() {
            SMTPResponseCallback callback = WelcomeResponseCallback.INSTANCE;
            
            @Override
            public void onResponse(SMTPClientSession session, SMTPResponse response) {
                initSession(session);
                callback.onResponse(session, response);
            }
            
            @Override
            public void onException(SMTPClientSession session, Throwable t) {
                initSession(session);
                callback.onException(session, t);
            }
            
            /**
             * Init the SMTPClienSesion by adding all needed data to the attributes
             * 
             * @param session
             */
            private void initSession(SMTPClientSession session) {
                Map<String, Object> attrs = session.getAttributes();
                
                Iterator<SMTPDeliveryTransaction> transactionIt = new ArrayIterator<SMTPDeliveryTransaction>(transactions);
                attrs.put(SMTP_TRANSACTIONS_KEY, transactionIt);
                SMTPDeliveryTransaction transaction = transactionIt.next();
                attrs.put(CURRENT_SMTP_TRANSACTION_KEY, transaction);
                attrs.put(RECIPIENTS_KEY, transaction.getRecipients().iterator());

                attrs.put(FUTURE_KEY, future);
                attrs.put(DELIVERY_STATUS_KEY, new ArrayList<DeliveryRecipientStatus>());
                attrs.put(DELIVERY_RESULT_LIST_KEY, new ArrayList<DeliveryResult>());
            }
        });
        
       
        return future;
    }
}
