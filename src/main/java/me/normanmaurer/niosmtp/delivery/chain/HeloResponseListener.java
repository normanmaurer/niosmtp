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

import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link me.normanmaurer.niosmtp.delivery.chain.ChainedSMTPClientFutureListener} implementation which will handle the <code>HELO</code> {@link me.normanmaurer.niosmtp.SMTPResponse}
 *
 * It will write the next {@link me.normanmaurer.niosmtp.SMTPRequest} to the {@link me.normanmaurer.niosmtp.transport.SMTPClientSession} with the right {@link me.normanmaurer.niosmtp.SMTPClientFutureListener} added.
 * 
 * HELO does not support PIPELINING, STARTTLS, or AUTH, however if any of these are configured niosmtp would have bailed out
 * at the EHLO response (see {@link EhloResponseListener}.
 *
 * @author Raman Gupta
 *
 */
public class HeloResponseListener extends ChainedSMTPClientFutureListener<SMTPResponse> implements SMTPClientConstants{


    /**
     * Get instance of this {@link me.normanmaurer.niosmtp.delivery.chain.HeloResponseListener} implementation.
     */
    public static final HeloResponseListener INSTANCE = new HeloResponseListener();

    private HeloResponseListener() {

    }

    @Override
    public void onResult(SMTPClientSession session, SMTPResponse response) throws SMTPException {
        int code = response.getCode();

        String mail = ((SMTPDeliveryEnvelope)session.getAttribute(CURRENT_SMTP_TRANSACTION_KEY)).getSender();

        if (code < 400) {
            next(session, SMTPRequestImpl.mail(mail));
        } else {
            setDeliveryStatusForAll(session, response);

        }

    }

}
