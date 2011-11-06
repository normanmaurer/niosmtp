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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.ArrayIterator;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.delivery.callback.AbstractResponseCallback;
import me.normanmaurer.niosmtp.delivery.callback.SMTPResponseCallbackFactory;
import me.normanmaurer.niosmtp.delivery.callback.SMTPResponseCallbackFactoryImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;



/**
 * {@link SMTPDeliveryAgent} which use the wrapped {@link SMTPClientTransport} to deliver email
 *  via SMTP
 * 
 * @author Norman Maurer
 * 
 */
public class SMTPDeliveryAgent implements SMTPClientConstants, SMTPDeliverySessionConstants {

    private final SMTPClientTransport transport;
    private final static SMTPResponseCallbackFactory FACTORY = new SMTPResponseCallbackFactoryImpl();
    
    public SMTPDeliveryAgent(final SMTPClientTransport transport) {
        this.transport = transport;
    }

    
    


    /**
     * Deliver the given {@link SMTPDeliveryEnvelope}'s 
     * 
     * The implementation may choose to do the deliver in an async fashion. 
     * 
     * @param host
     * @param config
     * @param transation
     * @return future
     */
    public SMTPClientFuture<Collection<DeliveryResult>> deliver(InetSocketAddress host, final SMTPDeliveryAgentConfig config, final SMTPDeliveryEnvelope... transactions) {
        if (transactions == null || transactions.length == 0) {
            throw new IllegalArgumentException("SMTPTransaction parameter must be not null and the length must be > 0");
        }

        final SMTPClientFutureImpl<Collection<DeliveryResult>> future = new SMTPClientFutureImpl<Collection<DeliveryResult>>();
        final SMTPResponseCallbackFactory callbackfactory = createFactory();
        
        transport.connect(host, config,new AbstractResponseCallback() {
            @Override
            public void onResponse(SMTPClientSession session, SMTPResponse response) throws Exception{
                initSession(session);
                callbackfactory.getCallback(session).onResponse(session, response);
            }

            @Override
            public void onException(SMTPClientSession session, Throwable t) {
                initSession(session);
                super.onException(session, t);
            
            }
           
            /**
             * Init the SMTPClienSesion by adding all needed data to the attributes
             * 
             * @param session
             */
            private void initSession(SMTPClientSession session) {
                Map<String, Object> attrs = session.getAttributes();
                
                Iterator<SMTPDeliveryEnvelope> transactionIt = new ArrayIterator<SMTPDeliveryEnvelope>(transactions);
                attrs.put(SMTP_TRANSACTIONS_KEY, transactionIt);
                SMTPDeliveryEnvelope transaction = transactionIt.next();
                attrs.put(CURRENT_SMTP_TRANSACTION_KEY, transaction);
                attrs.put(RECIPIENTS_KEY, transaction.getRecipients().iterator());

                attrs.put(FUTURE_KEY, future);
                attrs.put(DELIVERY_STATUS_KEY, new ArrayList<DeliveryRecipientStatus>());
                attrs.put(DELIVERY_RESULT_LIST_KEY, new ArrayList<DeliveryResult>());
                attrs.put(SMTP_RESPONSE_CALLBACK_FACTORY, createFactory());
            }
        });
        
       
        return future;
    }
    

    
    /**
     * Return the {@link SMTPResponseCallbackFactory} to use
     * 
     * @return factory
     */
    protected SMTPResponseCallbackFactory createFactory() {
        return FACTORY;
    }
}
