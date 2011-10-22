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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Iterator;

import me.normanmaurer.niosmtp.Authentication;
import me.normanmaurer.niosmtp.core.AuthenticationImpl;
import me.normanmaurer.niosmtp.core.SimpleMessageInput;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.delivery.DeliveryResult;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgent;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryFuture;
import me.normanmaurer.niosmtp.delivery.impl.SMTPDeliveryTransactionImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.util.TestUtils;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.hook.AuthHook;
import org.apache.james.protocols.smtp.hook.Hook;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.HookReturnCode;
import org.apache.james.protocols.smtp.hook.SimpleHook;
import org.junit.Test;

public abstract class AbstractSMTPClientAuthLoginTest extends AbstractSMTPClientTest{

    protected final static String VALID_USER ="myuser";
    protected final static String VALID_PASS ="mypassword";

    @Override
    protected NettyServer create(Hook hook) throws WiringException {
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        SMTPProtocolHandlerChain chain = new SMTPProtocolHandlerChain();
        chain.addHook(hook);
        chain.addHook(new TestAuthHook());
        return new NettyServer(new SMTPProtocol(chain, config));
    }

    @Override
    protected SMTPDeliveryAgentConfigImpl createConfig() {
        SMTPDeliveryAgentConfigImpl config =  super.createConfig();
        config.setAuthentication(createAuthentication(VALID_USER, VALID_PASS));
        return config;
    }
    
    protected Authentication createAuthentication(String username, String password) {
        return AuthenticationImpl.login(username, password);
    }

    @Test
    public void testRejectBecauseOfBadAuth() throws Exception {
        int port = TestUtils.getFreePort();

        NettyServer smtpServer = create(new SimpleHook());
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();

       
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = new SMTPDeliveryAgent(transport);

        try {
            SMTPDeliveryAgentConfigImpl conf = createConfig();
            conf.setAuthentication(createAuthentication("myuser", "mybadpassword"));

            SMTPDeliveryFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPDeliveryTransactionImpl("from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
            DeliveryResult dr = future.get().next();
            assertTrue(dr.isSuccess());
            assertNull(dr.getException());
            Iterator<DeliveryRecipientStatus> it = dr.getRecipientStatus();
            DeliveryRecipientStatus status = it.next();
            assertEquals(DeliveryRecipientStatus.DeliveryStatus.PermanentError, status.getStatus());
            assertEquals(535, status.getResponse().getCode());

            status = it.next();
            assertEquals(DeliveryRecipientStatus.DeliveryStatus.PermanentError, status.getStatus());
            assertEquals(535, status.getResponse().getCode());
            
            assertFalse(it.hasNext());
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    
    
    protected class TestAuthHook implements AuthHook {
        
        @Override
        public HookResult doAuth(SMTPSession session, String username, String password) {
            if (username.equals(VALID_USER) && password.equals(VALID_PASS)) {
                return new HookResult(HookReturnCode.OK);
            }
            return new HookResult(HookReturnCode.DECLINED);
        }
    }

}
