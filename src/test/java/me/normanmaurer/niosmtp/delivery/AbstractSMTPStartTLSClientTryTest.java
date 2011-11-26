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
package me.normanmaurer.niosmtp.delivery;

import java.util.List;

import me.normanmaurer.niosmtp.transport.SMTPClientTransport;

import org.apache.james.protocols.api.Encryption;
import org.apache.james.protocols.api.handler.ProtocolHandler;
import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.netty.NettyServer;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.core.esmtp.StartTlsCmdHandler;
import org.apache.james.protocols.smtp.hook.Hook;


public abstract class AbstractSMTPStartTLSClientTryTest extends AbstractSMTPStartTLSClientTest{

    @Override
    protected NettyServer create(Hook hook) throws WiringException {
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
             
        SMTPProtocolHandlerChain chain = new SMTPProtocolHandlerChain(hook) {

            @Override
            protected List<ProtocolHandler> initDefaultHandlers() {
                List<ProtocolHandler> defaultHandlers =  super.initDefaultHandlers();
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
        return new NettyServer(new SMTPProtocol(chain, config), Encryption.createStartTls(BogusSslContextFactory.getServerContext()));
        
    }

    @Override
    protected SMTPClientTransport createSMTPClient() {
        return createFactory().createStartTLS(BogusSslContextFactory.getClientContext(), false);

    }

}
