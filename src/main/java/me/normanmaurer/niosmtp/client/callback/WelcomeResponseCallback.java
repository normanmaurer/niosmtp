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
import java.util.LinkedList;
import java.util.List;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.client.DeliveryResultImpl;
import me.normanmaurer.niosmtp.client.SMTPClientFutureImpl;
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

    private SMTPClientConfig config;
    private LinkedList<String> recipients;
    private List<DeliveryRecipientStatus> statusList = new ArrayList<DeliveryRecipientStatus> ();
    private String mailFrom;
    private MessageInput msg;
    
    public WelcomeResponseCallback(SMTPClientFutureImpl future, final String mailFrom, final LinkedList<String> recipients, final MessageInput msg,  final SMTPClientConfig config) {
        super(future);
        this.config = config;
        this.recipients = recipients;
        this.msg = msg;
        this.mailFrom = mailFrom;
    }
    
    
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) {
        int code = response.getCode();
        if (code < 400) {
            session.send(SMTPRequestImpl.ehlo(config.getHeloName()), new EhloResponseCallback(future, statusList, mailFrom, recipients, msg, config));
/*
            if (context == null) {
            } else {
                // stateMachine.nextState(SMTPState.STARTTLS);

            }
            */
        } else {
            while (!recipients.isEmpty()) {
                statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
            }
            future.setDeliveryStatus(new DeliveryResultImpl(statusList));

            session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
            session.close();

        }            
    }
    
}
