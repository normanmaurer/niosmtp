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
import java.util.Iterator;
import java.util.Locale;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPPipeliningRequest;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.ReadySMTPClientFuture;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.impl.FutureResultImpl;

public class SMTPClientFutureListenerFactoryImpl implements SMTPClientFutureListenerFactory{

    private final static SMTPClientFutureListener<FutureResult<SMTPResponse>> EMPTY = new SMTPClientFutureListener<FutureResult<SMTPResponse>>() {

        @Override
        public void operationComplete(SMTPClientFuture<FutureResult<SMTPResponse>> future) {            
        }
    };
    @Override
    public SMTPClientFutureListener<FutureResult<SMTPResponse>> getListener(SMTPClientSession session, SMTPRequest request) throws SMTPException {

        String cmd = request.getCommand().toUpperCase(Locale.UK);
        String arg = request.getArgument();
        if (arg != null) {
            arg = arg.toUpperCase(Locale.UK);
        }
        if (SMTPRequest.EHLO_COMMAND.equals(cmd)) {
            return EhloResponseListener.INSTANCE;
        } else if (SMTPRequest.MAIL_COMMAND.equals(cmd)) {
            return MailResponseListener.INSTANCE;
        } else if (SMTPRequest.RCPT_COMMAND.equals(cmd)) {
            return RcptResponseListener.INSTANCE;
        } else if (SMTPRequest.DATA_COMMAND.equals(cmd)) {
            return DataResponseCallback.INSTANCE;
        } else if (SMTPRequest.STARTTLS_COMMAND.equals(cmd)) {
            return StartTlsResponseListener.INSTANCE;
        } else if (SMTPRequest.AUTH_COMMAND.equals(cmd) && arg != null) {
            if (arg.equals(SMTPRequest.AUTH_PLAIN_ARGUMENT)) {
                return AuthPlainResponseListener.INSTANCE;
            } else if (arg.equals(SMTPRequest.AUTH_LOGIN_ARGUMENT)) {
                return AuthLoginResponseListener.INSTANCE;
            }
        } else if (SMTPRequest.QUIT_COMMAND.equals(cmd)) {
            return EMPTY;
        }

        throw new SMTPException("No valid callback found for request " + request);
    }

    @Override
    public SMTPClientFutureListener<FutureResult<SMTPResponse>> getListener(SMTPClientSession session) throws SMTPException {
        return WelcomeResponseListener.INSTANCE;
    }

    @Override
    public SMTPClientFutureListener<FutureResult<Collection<SMTPResponse>>> getListener(SMTPClientSession session, SMTPMessage input) throws SMTPException {
        return PostDataResponseListener.INSTANCE;
    }

    @Override
    public SMTPClientFutureListener<FutureResult<Collection<SMTPResponse>>> getListener(SMTPClientSession session, final SMTPPipeliningRequest request) throws SMTPException {
        
        return new ChainedSMTPClientFutureListener<Collection<SMTPResponse>>() {
            private final Iterator<SMTPRequest> requests = request.getRequests().iterator();
            
            @Override
            protected void onResult(SMTPClientSession session, Collection<SMTPResponse> result) throws SMTPException {
                Iterator<SMTPResponse> responses = result.iterator();
                while(responses.hasNext()) {
                    FutureResult<SMTPResponse> fResult = new FutureResultImpl<SMTPResponse>(responses.next());
                    ReadySMTPClientFuture<FutureResult<SMTPResponse>> future = new ReadySMTPClientFuture<FutureResult<SMTPResponse>>(session, fResult);
                    future.addListener(getListener(session, requests.next()));
                }
            }
        };
        
    }
    
    

}
