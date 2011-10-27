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

import java.util.List;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.lmtp.LMTPConfigurationImpl;
import org.apache.james.protocols.lmtp.LMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.core.esmtp.StartTlsCmdHandler;
import org.apache.james.protocols.smtp.hook.Hook;
import org.apache.james.protocols.smtp.hook.SimpleHook;

import me.normanmaurer.niosmtp.delivery.AbstractSMTPStartTLSClientTest;
import me.normanmaurer.niosmtp.delivery.BogusSslContextFactory;
import me.normanmaurer.niosmtp.delivery.LMTPDeliveryAgent;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgent;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;

public abstract class AbstractLMTPStartTLSClientTest extends AbstractSMTPStartTLSClientTest{

    @Override
    protected SMTPDeliveryAgent createAgent(SMTPClientTransport transport) {
        return new LMTPDeliveryAgent(transport);
    }

    @Override
    protected NettyServer create(Hook hook) throws WiringException {
        LMTPProtocolHandlerChain chain = new LMTPProtocolHandlerChain() {

            @Override
            protected List<Object> initDefaultHandlers() {
                List<Object> handlers =  super.initDefaultHandlers();
                handlers.add(new StartTlsCmdHandler());
                return handlers;
            }
            
        };
        if (hook instanceof SimpleHook) {
            hook = new SimpleHookAdapter((SimpleHook)hook);
        }
        chain.addHook(hook);
        return new NettyServer(new SMTPProtocol(chain, new LMTPConfigurationImpl() {

            @Override
            public boolean isStartTLSSupported() {
                return true;
            }
            
        }), BogusSslContextFactory.getServerContext());
    }
}
