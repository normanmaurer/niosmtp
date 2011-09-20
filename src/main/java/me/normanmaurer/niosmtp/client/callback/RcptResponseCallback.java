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

import java.util.LinkedList;
import java.util.List;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;

import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatusImpl;
import me.normanmaurer.niosmtp.client.DeliveryResultImpl;
import me.normanmaurer.niosmtp.client.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus.Status;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;


/**
 * {@link AbstractResponseCallback} implementation which will handle the <code>RCPT</code> {@link SMTPResponse}
 * 
 * It will write the next {@link SMTPRequest} to the {@link SMTPClientSession} with the right {@link SMTPResponseCallback} added.
 * 
 * This implementation also handles the <code>PIPELINING</code> extension
 * 
 * @author Norman Maurer
 *
 */
public class RcptResponseCallback extends AbstractResponseCallback implements ResponseCallbackConstants {
    private SMTPClientConfig config;
    private LinkedList<String> recipients;
    private List<DeliveryRecipientStatus> statusList;
    private MessageInput msg;
    private String rcpt;

    public RcptResponseCallback(SMTPClientFutureImpl future, final List<DeliveryRecipientStatus> statusList, final LinkedList<String> recipients, final MessageInput msg,  final String rcpt, final SMTPClientConfig config) {
        super(future);
        this.config = config;
        this.recipients = recipients;
        this.msg = msg;
        this.statusList = statusList;
        this.rcpt = rcpt;
    }
    
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) {
        statusList.add(new DeliveryRecipientStatusImpl(rcpt, response));
        if (!recipients.isEmpty()) {
            String rcpt = recipients.removeFirst();

            // only write the request if the SMTPServer does not support
            // PIPELINING and we don't want to use it
            // as otherwise we already sent this
            if (!session.getAttributes().containsKey(PIPELINING_ACTIVE_KEY) || config.getPipeliningMode() == PipeliningMode.NO) {
                session.send(SMTPRequestImpl.rcpt(rcpt), new RcptResponseCallback(future, statusList, recipients, msg, rcpt, config));
            }
        } else {

            boolean success = false;
            for (int i = 0; i < statusList.size(); i++) {
                if (statusList.get(i).getStatus() == Status.Ok) {
                    success = true;
                    break;
                }
            }
            if (success) {
                // only write the request if the SMTPServer does not support
                // PIPELINING and we don't want to use it
                // as otherwise we already sent this
                if (!session.getAttributes().containsKey(PIPELINING_ACTIVE_KEY) || config.getPipeliningMode() == PipeliningMode.NO) {
                    session.send(SMTPRequestImpl.data(), new DataResponseCallback(future, statusList, msg));
                }

            } else {

                // all recipients failed so we should now complete the
                // future
                future.setDeliveryStatus(new DeliveryResultImpl(statusList));
                
                session.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                session.close();
            }
        }
    }

}
