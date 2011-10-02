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

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import me.normanmaurer.niosmtp.Authentication;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.client.DeliveryResultImpl;
import me.normanmaurer.niosmtp.client.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

public class AuthPlainResponseCallback extends AbstractResponseCallback{
    private final static SMTPResponseCallback INSTANCE = new AuthPlainResponseCallback();
    
    private final static Charset CHARSET = Charset.forName("US_ASCII");
    private final static String PROCESS_AUTH = "PROCESS_AUTH";

    @SuppressWarnings("unchecked")
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) {
        SMTPClientFutureImpl future = (SMTPClientFutureImpl) session.getAttributes().get(FUTURE_KEY);
        LinkedList<String> recipients = (LinkedList<String>) session.getAttributes().get(RECIPIENTS_KEY);
        List<DeliveryRecipientStatus> statusList = (List<DeliveryRecipientStatus>) session.getAttributes().get(DELIVERY_STATUS_KEY);
        
        if (session.getAttributes().containsKey(PROCESS_AUTH)) {
            if (response.getCode() == 235) {
                String mail = (String) session.getAttributes().get(SENDER_KEY);

                session.send(SMTPRequestImpl.mail(mail), MailResponseCallback.INSTANCE);
            } else {
                while (!recipients.isEmpty()) {
                    statusList.add(new DeliveryRecipientStatusImpl(recipients.removeFirst(), response));
                }

                future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                session.close();
            }
        } else {
            if (response.getCode() == 334) {
                session.getAttributes().put(PROCESS_AUTH, true);
                Authentication auth = session.getConfig().getAuthentication();
                String userPass = Base64.encodeBase64String(auth.getUsername().getBytes(CHARSET)) + "\0" + Base64.encodeBase64String(auth.getPassword().getBytes(CHARSET));
                session.send(new SMTPRequestImpl(userPass, null), INSTANCE);
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

}
