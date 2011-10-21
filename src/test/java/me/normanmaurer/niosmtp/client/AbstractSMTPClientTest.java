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
package me.normanmaurer.niosmtp.client;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Iterator;


import me.normanmaurer.niosmtp.SMTPConnectionException;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.client.DeliveryResult;
import me.normanmaurer.niosmtp.client.SMTPDeliveryAgent;
import me.normanmaurer.niosmtp.client.SMTPDeliveryFuture;
import me.normanmaurer.niosmtp.client.SMTPDeliveryTransaction;
import me.normanmaurer.niosmtp.client.impl.SMTPDeliveryTransactionImpl;
import me.normanmaurer.niosmtp.core.SMTPClientConfigImpl;
import me.normanmaurer.niosmtp.core.SimpleMessageInput;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.SMTPClientTransportFactory;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.hook.Hook;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.HookReturnCode;
import org.apache.james.protocols.smtp.hook.SimpleHook;
import org.apache.mailet.MailAddress;
import org.junit.Test;


public abstract class AbstractSMTPClientTest {
    

    protected NettyServer create(Hook hook) throws WiringException {
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        SMTPProtocolHandlerChain chain = new SMTPProtocolHandlerChain();
        chain.addHook(hook);
        return new NettyServer(new SMTPProtocol(chain, config));
        
    }
    protected abstract SMTPClientTransportFactory createFactory();
    
    protected SMTPClientTransport createSMTPClient() {
        return createFactory().createPlain();
    }
    
    protected SMTPClientConfigImpl createConfig() {
        SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
        conf.setConnectionTimeout(4);
        conf.setResponseTimeout(5);
        return conf;
    }
    
    @Test
    public void testRejectMailFrom() throws Exception {
        checkRejectMailFrom(new RejectMailFromAssertCheck());
    }
    
    
    @Test
    public void testRejectMailFromNonBlocking() throws Exception {
        checkRejectMailFrom(new AsyncAssertCheck(new RejectMailFromAssertCheck()));
    }
    
    
    private void checkRejectMailFrom(AssertCheck check) throws Exception {
        int port = TestUtils.getFreePort();

        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult doMail(SMTPSession session, MailAddress sender) {
                return new HookResult(HookReturnCode.DENY);
            }
            
        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();

       
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = new SMTPDeliveryAgent(transport);

        try {
            SMTPClientConfigImpl conf = createConfig();
            SMTPDeliveryFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPDeliveryTransactionImpl("from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
            check.onSMTPClientFuture(future);
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    
    private final class RejectMailFromAssertCheck extends AssertCheck {

        @Override
        protected void onDeliveryResult(Iterator<DeliveryResult> result) {
            DeliveryResult dr = result.next();
            assertTrue(dr.isSuccess());
            assertNull(dr.getException());
            Iterator<DeliveryRecipientStatus> it = dr.getRecipientStatus();
            DeliveryRecipientStatus status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());

            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());
            
            assertFalse(it.hasNext());
        }
        
    }
    
    
    
    
    @Test
    public void testRejectHelo() throws Exception{
        checkRejectHelo(new RejectHeloAssertCheck());
    }
    
    @Test
    public void testRejectHeloNonBlocking() throws Exception{
        checkRejectHelo(new AsyncAssertCheck(new RejectHeloAssertCheck()));
    }
    
    
    private void checkRejectHelo(AssertCheck check) throws Exception{
        int port = TestUtils.getFreePort();

        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult doHelo(SMTPSession session, String helo) {
                return new HookResult(HookReturnCode.DENY);
            }

            
        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();


       
        
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = new SMTPDeliveryAgent(transport);


        try {
            SMTPClientConfigImpl conf = createConfig();
            SMTPDeliveryFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPDeliveryTransactionImpl("from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
            check.onSMTPClientFuture(future);
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    
    private final class RejectHeloAssertCheck extends AssertCheck {

        @Override
        protected void onDeliveryResult(Iterator<DeliveryResult> result) {
            DeliveryResult dr = result.next();
            assertTrue(dr.isSuccess());
            assertNull(dr.getException());
            Iterator<DeliveryRecipientStatus> it = dr.getRecipientStatus();
            DeliveryRecipientStatus status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());

            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());
            
            assertFalse(it.hasNext());            
        }
        
    }
    
    
    
    @Test
    public void testRejectAllRecipients() throws Exception {
        checkRejectAllRecipients(new RejectAllRecipientsAssertCheck());
    }
    
    @Test
    public void testRejectAllRecipientsNonBlocking() throws Exception {
        checkRejectAllRecipients(new AsyncAssertCheck(new RejectAllRecipientsAssertCheck()));
    }
    
    
    private void checkRejectAllRecipients(AssertCheck check) throws Exception {
        int port = TestUtils.getFreePort();


        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult doRcpt(SMTPSession session, MailAddress sender, MailAddress rcpt) {
                return new HookResult(HookReturnCode.DENY);
            }
            
            
        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();



       
        
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = new SMTPDeliveryAgent(transport);

        try {
            SMTPClientConfigImpl conf = createConfig();

            SMTPDeliveryFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPDeliveryTransactionImpl("from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
            check.onSMTPClientFuture(future);
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    
    private final class RejectAllRecipientsAssertCheck extends AssertCheck {

        @Override
        protected void onDeliveryResult(Iterator<DeliveryResult> result) {
            DeliveryResult dr = result.next();
            assertTrue(dr.isSuccess());
            assertNull(dr.getException());
            Iterator<DeliveryRecipientStatus> it = dr.getRecipientStatus();
            DeliveryRecipientStatus status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());

            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());
            
            assertFalse(it.hasNext());
        }
        
    }
    
    @Test
    public void testRejectData() throws Exception {
        checkRejectData(new RejectDataAssertCheck());
    }
    
    @Test
    public void testRejectDataNonBlocking() throws Exception {
        checkRejectData(new AsyncAssertCheck(new RejectDataAssertCheck()));
    }
    
    
    private void checkRejectData(AssertCheck check) throws Exception {
        int port = TestUtils.getFreePort();

        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult onMessage(SMTPSession session, MailEnvelope mail) {
                return new HookResult(HookReturnCode.DENY);
            }


        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();


       
        
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = new SMTPDeliveryAgent(transport);

        try {
            SMTPClientConfigImpl conf = createConfig();

            SMTPDeliveryFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPDeliveryTransactionImpl("from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
            check.onSMTPClientFuture(future);
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    
    private final class RejectDataAssertCheck extends AssertCheck {

        @Override
        protected void onDeliveryResult(Iterator<DeliveryResult> result) {
            DeliveryResult dr = result.next();
            assertTrue(dr.isSuccess());
            assertNull(dr.getException());
            Iterator<DeliveryRecipientStatus> it = dr.getRecipientStatus();
            DeliveryRecipientStatus status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());

            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());
            
            assertFalse(it.hasNext());
        }
        
    }
    
    @Test
    public void testRejectOneRecipient() throws Exception {
        checkRejectOneRecipient(new RejectOneRecipientAssertCheck());
    }
    
    @Test
    public void testRejectOneRecipientNonBlocking() throws Exception {
        checkRejectOneRecipient(new AsyncAssertCheck(new RejectOneRecipientAssertCheck()));
    }
    
    
    private void checkRejectOneRecipient(AssertCheck check) throws Exception {
        int port = TestUtils.getFreePort();


        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult doRcpt(SMTPSession session, MailAddress sender, MailAddress rcpt) {
                if (rcpt.toString().equals("to2@example.com"))  {
                    return new HookResult(HookReturnCode.DENY);
                } else {
                    return super.doRcpt(session, sender, rcpt);
                }
            }

        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();


       
        
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = new SMTPDeliveryAgent(transport);

        try {
            SMTPClientConfigImpl conf = createConfig();

            SMTPDeliveryFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPDeliveryTransactionImpl("from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com", "to3@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
            check.onSMTPClientFuture(future);
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
 
    private final class RejectOneRecipientAssertCheck extends AssertCheck {

        @Override
        protected void onDeliveryResult(Iterator<DeliveryResult> result) {
            DeliveryResult dr = result.next();
            assertTrue(dr.isSuccess());
            assertNull(dr.getException());
            Iterator<DeliveryRecipientStatus> it = dr.getRecipientStatus();
            DeliveryRecipientStatus status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.Ok, status.getStatus());
            assertEquals(250, status.getResponse().getCode());
            assertEquals("to@example.com", status.getAddress());
            
            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.PermanentError, status.getStatus());
            assertEquals(554, status.getResponse().getCode());
            assertEquals("to2@example.com", status.getAddress());

            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.Ok, status.getStatus());
            assertEquals(250, status.getResponse().getCode());
            assertEquals("to3@example.com", status.getAddress());
            
            assertFalse(it.hasNext());            
        }
        
    }
    
    
    @Test
    public void testMultiplePerConnection() throws Exception {
        checkMultiplePerConnection(new MultiplePerConnectionAssertCheck());
    }
    
    @Test
    public void testMultiplePerConnectionNonBlocking() throws Exception {
        checkMultiplePerConnection(new AsyncAssertCheck(new MultiplePerConnectionAssertCheck()));
    }
    
    
    private void checkMultiplePerConnection(AssertCheck check) throws Exception {
        int port = TestUtils.getFreePort();


        NettyServer smtpServer = create(new SimpleHook() {

        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();


       
        
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = new SMTPDeliveryAgent(transport);

        try {
            SMTPClientConfigImpl conf = createConfig();
            SMTPDeliveryTransaction transaction = new SMTPDeliveryTransactionImpl("from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com", "to3@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes())));
            
            SMTPDeliveryFuture future = c.deliver(new InetSocketAddress(port), conf, new SMTPDeliveryTransaction[] {transaction, transaction});
            check.onSMTPClientFuture(future);
            
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
 
    private final class MultiplePerConnectionAssertCheck extends AssertCheck {

        @Override
        protected void onDeliveryResult(Iterator<DeliveryResult> results) {
            
            DeliveryResult dr = results.next();
            
            
            assertTrue(dr.isSuccess());
            assertNull(dr.getException());
            Iterator<DeliveryRecipientStatus> it = dr.getRecipientStatus();
            DeliveryRecipientStatus status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.Ok, status.getStatus());
            assertEquals(250, status.getResponse().getCode());
            assertEquals("to@example.com", status.getAddress());
            
            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.Ok, status.getStatus());
            assertEquals(250, status.getResponse().getCode());
            assertEquals("to2@example.com", status.getAddress());

            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.Ok, status.getStatus());
            assertEquals(250, status.getResponse().getCode());
            assertEquals("to3@example.com", status.getAddress());
            
            assertFalse(it.hasNext());
            
            
            dr = results.next();
            assertTrue(dr.isSuccess());
            assertNull(dr.getException());
            it = dr.getRecipientStatus();
            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.Ok, status.getStatus());
            assertEquals(250, status.getResponse().getCode());
            assertEquals("to@example.com", status.getAddress());
            
            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.Ok, status.getStatus());
            assertEquals(250, status.getResponse().getCode());
            assertEquals("to2@example.com", status.getAddress());

            status = it.next();
            assertEquals(DeliveryRecipientStatus.Status.Ok, status.getStatus());
            assertEquals(250, status.getResponse().getCode());
            assertEquals("to3@example.com", status.getAddress());
            
            assertFalse(it.hasNext());
            assertFalse(results.hasNext());            
        }
        
    }
    
    @Test
    public void testConnectionRefused() throws Exception {
        checkConnectionRefused(new ConnectionRefusedAssertCheck());
    }
    
    @Test
    public void testConnectionRefusedNonBlocking() throws Exception {
        checkConnectionRefused(new AsyncAssertCheck(new ConnectionRefusedAssertCheck()));
    }
    
    
    private void checkConnectionRefused(AssertCheck check) throws Exception {
        
        SMTPClientTransport transport = createSMTPClient();
        SMTPDeliveryAgent c = new SMTPDeliveryAgent(transport);

        SMTPClientConfigImpl conf = createConfig();

        SMTPDeliveryFuture future = c.deliver(new InetSocketAddress(11111), conf, new SMTPDeliveryTransactionImpl("from@example.com", Arrays.asList(new String[] { "to@example.com" }), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes()))));
        try {
            check.onSMTPClientFuture(future);
        } finally {
            transport.destroy();
        }
    }
    
    
    private final class ConnectionRefusedAssertCheck extends AssertCheck {

        @Override
        protected void onDeliveryResult(Iterator<DeliveryResult> result) {
            DeliveryResult dr = result.next();
            assertFalse(dr.isSuccess());
            assertNull(dr.getRecipientStatus());
            assertEquals(SMTPConnectionException.class, dr.getException().getClass());            
        }
        
    }


}
