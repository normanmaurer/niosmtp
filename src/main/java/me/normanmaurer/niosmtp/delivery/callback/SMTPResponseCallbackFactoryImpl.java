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
package me.normanmaurer.niosmtp.delivery.callback;

import java.util.Locale;

import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

public class SMTPResponseCallbackFactoryImpl implements SMTPResponseCallbackFactory{

    @Override
    public SMTPResponseCallback getCallback(SMTPClientSession session, SMTPRequest request) throws SMTPException {

        String cmd = request.getCommand().toUpperCase(Locale.UK);
        String arg = request.getArgument();
        if (arg != null) {
            arg = arg.toUpperCase(Locale.UK);
        }
        if (SMTPRequest.EHLO_COMMAND.equals(cmd)) {
            return EhloResponseCallback.INSTANCE;
        } else if (SMTPRequest.MAIL_COMMAND.equals(cmd)) {
            return MailResponseCallback.INSTANCE;
        } else if (SMTPRequest.RCPT_COMMAND.equals(cmd)) {
            return RcptResponseCallback.INSTANCE;
        } else if (SMTPRequest.DATA_COMMAND.equals(cmd)) {
            return DataResponseCallback.INSTANCE;
        } else if (SMTPRequest.STARTTLS_COMMAND.equals(cmd)) {
            return StartTlsResponseCallback.INSTANCE;
        } else if (SMTPRequest.AUTH_COMMAND.equals(cmd) && arg != null) {
            if (arg.equals(SMTPRequest.AUTH_PLAIN_ARGUMENT)) {
                return AuthPlainResponseCallback.INSTANCE;
            } else if (arg.equals(SMTPRequest.AUTH_LOGIN_ARGUMENT)) {
                return AuthLoginResponseCallback.INSTANCE;
            }
        } else if (SMTPRequest.QUIT_COMMAND.equals(cmd)) {
            return SMTPResponseCallback.EMPTY;
        }

        throw new SMTPException("No valid callback found for request " + request);
    }

    @Override
    public SMTPResponseCallback getCallback(SMTPClientSession session) throws SMTPException {
        return WelcomeResponseCallback.INSTANCE;
    }

    @Override
    public SMTPResponseCallback getCallback(SMTPClientSession session, SMTPMessage input) throws SMTPException {
        return PostDataResponseCallback.INSTANCE;
    }

}
