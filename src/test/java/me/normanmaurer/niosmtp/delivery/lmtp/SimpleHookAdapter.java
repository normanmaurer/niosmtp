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
package me.normanmaurer.niosmtp.delivery.lmtp;

import org.apache.james.protocols.lmtp.hook.DeliverToRecipientHook;
import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.SimpleHook;
import org.apache.james.protocols.smtp.MailAddress;


public class SimpleHookAdapter extends SimpleHook implements DeliverToRecipientHook{

    private final SimpleHook hook;
    public SimpleHookAdapter(SimpleHook hook) {
        this.hook = hook;
    }
    
    @Override
    public HookResult doHelo(SMTPSession session, String helo) {
        return hook.doHelo(session, helo);
    }

    @Override
    public HookResult doMail(SMTPSession session, MailAddress sender) {
        return hook.doMail(session, sender);
    }

    @Override
    public HookResult doRcpt(SMTPSession session, MailAddress sender, MailAddress rcpt) {
        return hook.doRcpt(session, sender, rcpt);
    }

    @Override
    public HookResult onMessage(SMTPSession session, MailEnvelope mail) {
        return hook.onMessage(session, mail);
    }

    @Override
    public HookResult deliver(SMTPSession session, MailAddress arg1, MailEnvelope mail) {
        return onMessage(session, mail);
    }
    
}
