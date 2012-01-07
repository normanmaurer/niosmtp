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

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.core.SMTPMessageImpl;
import me.normanmaurer.niosmtp.delivery.AbstractSMTPSClientTest;
import me.normanmaurer.niosmtp.delivery.AssertCheck;
import me.normanmaurer.niosmtp.delivery.AsyncAssertCheck;
import me.normanmaurer.niosmtp.delivery.BogusSslContextFactory;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.delivery.LMTPDeliveryAgent;
import me.normanmaurer.niosmtp.delivery.MockLogger;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgent;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;
import me.normanmaurer.niosmtp.delivery.impl.SMTPDeliveryAgentConfigImpl;
import me.normanmaurer.niosmtp.delivery.impl.SMTPDeliveryEnvelopeImpl;
import me.normanmaurer.niosmtp.delivery.lmtp.AbstractLMTPClientTest.RejectOneRecipientAfterDataAssertCheck;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.util.TestUtils;

import org.apache.james.protocols.api.Encryption;
import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.netty.NettyServer;
import org.apache.james.protocols.lmtp.LMTPConfigurationImpl;
import org.apache.james.protocols.lmtp.LMTPProtocolHandlerChain;
import org.apache.james.protocols.lmtp.hook.DeliverToRecipientHook;
import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.hook.Hook;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.HookReturnCode;
import org.apache.james.protocols.smtp.hook.SimpleHook;
import org.apache.james.protocols.smtp.MailAddress;
import org.junit.Test;

public abstract class AbstractLMTPSClientTest extends AbstractSMTPSClientTest{

    @Override
    protected NettyServer create(Hook hook) throws WiringException {
        if (hook instanceof SimpleHook) {
            hook = new SimpleHookAdapter((SimpleHook)hook);
        }
        LMTPProtocolHandlerChain chain = new LMTPProtocolHandlerChain(hook);

        return new NettyServer(new SMTPProtocol(chain, new LMTPConfigurationImpl(), new MockLogger()), Encryption.createTls(BogusSslContextFactory.getServerContext()));
    
    }

    @Override
    protected SMTPClientTransport createSMTPClient() {
        return createFactory().createSMTPS(BogusSslContextFactory.getClientContext());
    }
    
    

    @Override
    protected SMTPDeliveryAgent createAgent(SMTPClientTransport transport) {
        return new LMTPDeliveryAgent(transport);
    }

    
    @Test
    public void testRejectOneRecipientAfterData() throws Exception {
        checkRejectOneRecipientAfterData(new RejectOneRecipientAfterDataAssertCheck());
    }
    
    @Test
    public void testRejectOneRecipientAfterDataNonBlocking() throws Exception {
        checkRejectOneRecipientAfterData(new AsyncAssertCheck(new RejectOneRecipientAfterDataAssertCheck()));
    }
    
    
    private void checkRejectOneRecipientAfterData(AssertCheck check) throws Exception {
        int port = TestUtils.getFreePort();
        

        NettyServer smtpServer = create(new DeliverToRecipientHook() {
            
            @Override
            public HookResult deliver(SMTPSession session, MailAddress address, MailEnvelope env) {
                if (address.toString().equals("to@example.com")) {
                    return new HookResult(HookReturnCode.DENY);
                }
                return new HookResult(HookReturnCode.OK);
            }
        });
        
        smtpServer.setListenAddresses(new InetSocketAddress(port));

        smtpServer.bind();


       
        
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = createAgent(transport);

        try {
            SMTPDeliveryAgentConfigImpl conf = createConfig();
            SMTPDeliveryEnvelope transaction = new SMTPDeliveryEnvelopeImpl("from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com", "to3@example.com"}), new SMTPMessageImpl(new ByteArrayInputStream("msg".getBytes())));
            
            SMTPClientFuture<Collection<FutureResult<Iterator<DeliveryRecipientStatus>>>> future = c.deliver(new InetSocketAddress(port), conf,transaction);
            check.onSMTPClientFuture(future);
            
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
}
