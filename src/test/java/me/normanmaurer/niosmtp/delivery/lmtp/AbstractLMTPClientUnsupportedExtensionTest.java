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

import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.james.protocols.api.Secure;
import org.apache.james.protocols.api.handler.ProtocolHandler;
import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.lmtp.LMTPConfiguration;
import org.apache.james.protocols.lmtp.LMTPConfigurationImpl;
import org.apache.james.protocols.lmtp.LMTPProtocolHandlerChain;
import org.apache.james.protocols.lmtp.LhloCmdHandler;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.esmtp.StartTlsCmdHandler;

import me.normanmaurer.niosmtp.delivery.AbstractSMTPClientUnsupportedExtensionTest;
import me.normanmaurer.niosmtp.delivery.LMTPDeliveryAgent;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgent;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;

public abstract class AbstractLMTPClientUnsupportedExtensionTest extends AbstractSMTPClientUnsupportedExtensionTest{

    protected NettyServer create() throws WiringException {
        LMTPConfiguration config = new LMTPConfigurationImpl();
        LMTPProtocolHandlerChain chain = new LMTPProtocolHandlerChain() {

            @Override
            protected List<ProtocolHandler> initDefaultHandlers() {
                List<ProtocolHandler> defaultHandlers =  super.initDefaultHandlers();
                for (int i = 0 ; i < defaultHandlers.size(); i++) {
                    Object h = defaultHandlers.get(i);
                    if (h instanceof LhloCmdHandler) {
                        defaultHandlers.remove(h);
                        defaultHandlers.add(new LhloCmdHandler() {

                            @SuppressWarnings("unchecked")
                            @Override
                            public List<String> getImplementedEsmtpFeatures(SMTPSession session) {
                                return Collections.EMPTY_LIST;
                            }
                            
                        });
                    }
                }
                return defaultHandlers;
            }
        };
        chain.wireExtensibleHandlers();

        return new NettyServer(new SMTPProtocol(chain, config));
    }
    
    protected NettyServer create(SSLContext context) throws WiringException {
        LMTPConfiguration config = new LMTPConfigurationImpl();
        
        LMTPProtocolHandlerChain chain = new LMTPProtocolHandlerChain() {

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
        chain.wireExtensibleHandlers();
        return new NettyServer(new SMTPProtocol(chain, config), Secure.createStartTls(context));
    }
    
    protected SMTPDeliveryAgent createAgent(SMTPClientTransport transport) {
        return new LMTPDeliveryAgent(transport);
    }
    
}
