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

import me.normanmaurer.niosmtp.delivery.AbstractSMTPClientAuthLoginTest;
import me.normanmaurer.niosmtp.delivery.LMTPDeliveryAgent;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgent;
import me.normanmaurer.niosmtp.delivery.impl.SMTPDeliveryAgentConfigImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.lmtp.LMTPConfigurationImpl;
import org.apache.james.protocols.lmtp.LMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.hook.Hook;
import org.apache.james.protocols.smtp.hook.SimpleHook;

public abstract class AbstractLMTPClientAuthLoginTest extends AbstractSMTPClientAuthLoginTest{

    @Override
    protected SMTPDeliveryAgent createAgent(SMTPClientTransport transport) {
        return new LMTPDeliveryAgent(transport);
    }

    @Override
    protected NettyServer create(Hook hook) throws WiringException {
        LMTPProtocolHandlerChain chain = new LMTPProtocolHandlerChain();
        if (hook instanceof SimpleHook) {
            hook = new SimpleHookAdapter((SimpleHook)hook);
        }
        chain.addHook(hook);
        chain.addHook(new TestAuthHook());
        return new NettyServer(new SMTPProtocol(chain, new LMTPConfigurationImpl()));
    }

    @Override
    protected SMTPDeliveryAgentConfigImpl createConfig() {
        SMTPDeliveryAgentConfigImpl config =  super.createConfig();
        config.setAuthentication(createAuthentication(VALID_USER, VALID_PASS));
        return config;
    }
   
}
