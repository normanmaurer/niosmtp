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
package me.normanmaurer.niosmtp;

import java.util.List;

import me.normanmaurer.niosmtp.transport.impl.NettySMTPClientTransport;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.core.esmtp.StartTlsCmdHandler;
import org.apache.james.protocols.smtp.hook.Hook;


public class SMTPStartTLSClientTryTest extends SMTPStartTLSClientTest{

    @Override
    protected NettyServer create(Hook hook) throws WiringException {
        SMTPConfigurationImpl config = new SMTPConfigurationImpl() {

            @Override
            public boolean isStartTLSSupported() {
                return true;
            }
            
        };
             
        SMTPProtocolHandlerChain chain = new SMTPProtocolHandlerChain() {

            @Override
            protected List<Object> initDefaultHandlers() {
                List<Object> defaultHandlers =  super.initDefaultHandlers();
                for (int i = 0 ; i < defaultHandlers.size(); i++) {
                    Object h = defaultHandlers.get(i);
                    if (h instanceof StartTlsCmdHandler) {
                        defaultHandlers.remove(h);
                        return defaultHandlers;
                    }
                }
                return defaultHandlers;
            }


            
        };
        chain.addHook(hook);
        return new NettyServer(new SMTPProtocol(chain, config),BogusSslContextFactory.getServerContext());
        
    }

    @Override
    protected NettySMTPClientTransport createSMTPClient() {
        return NettySMTPClientTransport.createStartTLS(BogusSslContextFactory.getClientContext(), false);

    }

}
