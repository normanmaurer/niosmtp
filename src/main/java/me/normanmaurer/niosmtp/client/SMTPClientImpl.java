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
package me.normanmaurer.niosmtp.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientConstants;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.client.callback.WelcomeResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;



/**
 * {@link SMTPClientImpl} which use the wrapped {@link SMTPClientTransport} to deliver email
 * 
 * So no pooling is active
 * 
 * @author Norman Maurer
 * 
 */
public class SMTPClientImpl implements SMTPClientConstants,SMTPClient, SMTPClientSessionConstants {

    private SMTPClientTransport transport;

    public SMTPClientImpl(SMTPClientTransport transport) {
        this.transport = transport;
    }

    
    


    @Override
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final Collection<String> recipients, final MessageInput msg, final SMTPClientConfig config) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient must be given");
        }

        final SMTPClientFutureImpl future = new SMTPClientFutureImpl();

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
                attrs.put(FUTURE_KEY, future);
                if (mailFrom == null) {
                    attrs.put(SENDER_KEY, "");
                } else {
                    attrs.put(SENDER_KEY, mailFrom);
                }
                attrs.put(RECIPIENTS_KEY, new LinkedList<String>(recipients));
                attrs.put(MSG_KEY, msg);
                attrs.put(DELIVERY_STATUS_KEY, new ArrayList<DeliveryRecipientStatus>());
            }
        });
        
       
        return future;
    }
}
