/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package me.normanmaurer.niosmtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.esmtp.EhloCmdHandler;
import org.apache.james.protocols.smtp.core.esmtp.StartTlsCmdHandler;
import org.junit.Test;

import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;
import me.normanmaurer.niosmtp.client.DeliveryResult;
import me.normanmaurer.niosmtp.client.SMTPClientFuture;
import me.normanmaurer.niosmtp.client.SMTPClientImpl;
import me.normanmaurer.niosmtp.client.SMTPTransactionImpl;
import me.normanmaurer.niosmtp.core.SMTPClientConfigImpl;
import me.normanmaurer.niosmtp.core.SimpleMessageInput;
import me.normanmaurer.niosmtp.transport.impl.NettySMTPClientTransport;


public class UnsuportedExtensionTest {

    protected SMTPClientConfigImpl createConfig() {
        SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
        conf.setConnectionTimeout(4);
        conf.setResponseTimeout(5);
        return conf;
    }


    @Test
    public void testDependOnPipelining() throws Exception {
        int port = 6028;

        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        SMTPProtocolHandlerChain chain = new SMTPProtocolHandlerChain() {

            @Override
            protected List<Object> initDefaultHandlers() {
                List<Object> defaultHandlers =  super.initDefaultHandlers();
                for (int i = 0 ; i < defaultHandlers.size(); i++) {
                    Object h = defaultHandlers.get(i);
                    if (h instanceof EhloCmdHandler) {
                        defaultHandlers.remove(h);
                        defaultHandlers.add(new EhloCmdHandler() {

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
        NettyServer smtpServer = new NettyServer(new SMTPProtocol(chain, config));
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));
        smtpServer.bind();

        
        NettySMTPClientTransport transport = NettySMTPClientTransport.createPlain();
        SMTPClientImpl c = new SMTPClientImpl(transport);

        SMTPClientConfigImpl conf = createConfig();
        conf.setPipeliningMode(PipeliningMode.DEPEND);

        SMTPClientFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPTransactionImpl("from@example.com", Arrays.asList(new String[] { "to@example.com" }), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
        try {
            
            // We expect to receive an exception as PIPELINING was not supported
            DeliveryResult dr = future.get().next();
            assertFalse(dr.isSuccess());
            assertNull(dr.getRecipientStatus());
            assertEquals(SMTPUnsupportedExtensionException.class, dr.getException().getClass());
        } finally {
            transport.destroy();
            smtpServer.unbind();
        }
    }
    
 
    @Test
    public void testDependOnStartTLS() throws Exception {
        int port = 6028;

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
        NettyServer smtpServer = new NettyServer(new SMTPProtocol(chain, config), BogusSslContextFactory.getServerContext());
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));
        
        smtpServer.bind();

        
        NettySMTPClientTransport transport = NettySMTPClientTransport.createStartTLS(BogusSslContextFactory.getClientContext(), true);
        SMTPClientImpl c = new SMTPClientImpl(transport);

        SMTPClientConfigImpl conf = createConfig();

        SMTPClientFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPTransactionImpl("from@example.com", Arrays.asList(new String[] { "to@example.com" }), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
        try {
            
            // We expect to receive an exception as STARTTLS was not supported
            DeliveryResult dr = future.get().next();
            assertFalse(dr.isSuccess());
            assertNull(dr.getRecipientStatus());
            assertEquals(SMTPUnsupportedExtensionException.class, dr.getException().getClass());
        } finally {
            transport.destroy();
            smtpServer.unbind();
        }
    }
    
}
