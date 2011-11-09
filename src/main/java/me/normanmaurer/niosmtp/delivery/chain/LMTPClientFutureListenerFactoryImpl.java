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

import java.util.Collection;
import java.util.Locale;

import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.LMTPRequest;
import me.normanmaurer.niosmtp.delivery.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;


/**
 * {@link SMTPClientFutureListenerFactoryImpl} implementation which returns the {@link SMTPClientFutureListener}'s while processing
 * the LMTP Transaction
 * 
 * @author Norman Maurer
 *
 */
public class LMTPClientFutureListenerFactoryImpl extends SMTPClientFutureListenerFactoryImpl {

    @Override
    public SMTPClientFutureListener<FutureResult<SMTPResponse>> getListener(SMTPClientSession session, SMTPRequest request) throws SMTPException {
        if (request == null) {
            return LMTPWelcomeResponseListener.INSTANCE;
        } else {
            String cmd = request.getCommand().toUpperCase(Locale.UK);
            if (LMTPRequest.LHLO_COMMAND.equals(cmd)) {
                return EhloResponseListener.INSTANCE;
            } else {
                return super.getListener(session, request);
            }
        }
    }

    @Override
    public SMTPClientFutureListener<FutureResult<SMTPResponse>> getListener(SMTPClientSession session) throws SMTPException {
        return LMTPWelcomeResponseListener.INSTANCE;

    }

    @Override
    public SMTPClientFutureListener<FutureResult<Collection<SMTPResponse>>> getListener(SMTPClientSession session, SMTPMessage input) throws SMTPException {
        return LMTPPostDataResponseCallback.INSTANCE;

    }


}
