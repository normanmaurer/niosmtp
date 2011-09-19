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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


import me.normanmaurer.niosmtp.SMTPClientConfig.PipeliningMode;
import me.normanmaurer.niosmtp.client.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.client.DeliveryResult;
import me.normanmaurer.niosmtp.client.SMTPClientImpl;
import me.normanmaurer.niosmtp.client.SMTPClientFuture;
import me.normanmaurer.niosmtp.core.SMTPClientConfigImpl;
import me.normanmaurer.niosmtp.core.SimpleMessageInput;
import me.normanmaurer.niosmtp.transport.impl.NettySMTPClientTransport;

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


public class SMTPClientTest {
    

    protected NettyServer create(Hook hook) throws WiringException {
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        SMTPProtocolHandlerChain chain = new SMTPProtocolHandlerChain();
        chain.addHook(hook);
        return new NettyServer(new SMTPProtocol(chain, config));
        
    }
    
    protected NettySMTPClientTransport createSMTPClient() {
        return NettySMTPClientTransport.createPlain();
    }
    
    @Test
    public void testRejectMailFrom() throws Exception {
        int port = 6028;

        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult doMail(SMTPSession session, MailAddress sender) {
                return new HookResult(HookReturnCode.DENY);
            }
            
        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();

       
        NettySMTPClientTransport transport = createSMTPClient();
        SMTPClientImpl c = new SMTPClientImpl(transport);

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes())), conf);
            DeliveryResult dr = future.get();
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
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }


    @Test
    public void testRejectHelo() throws Exception{
        int port = 6028;

        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult doHelo(SMTPSession session, String helo) {
                return new HookResult(HookReturnCode.DENY);
            }

            
        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();


       
        
        NettySMTPClientTransport transport = createSMTPClient();
        SMTPClientImpl c = new SMTPClientImpl(transport);


        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes())), conf);
            DeliveryResult dr = future.get();
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
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    @Test
    public void testRejectAllRecipients() throws Exception {
        int port = 6028;


        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult doRcpt(SMTPSession session, MailAddress sender, MailAddress rcpt) {
                return new HookResult(HookReturnCode.DENY);
            }
            
            
        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();



       
        
        NettySMTPClientTransport transport = createSMTPClient();
        SMTPClientImpl c = new SMTPClientImpl(transport);

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            conf.setPipeliningMode(PipeliningMode.NO);;
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes())), conf);
            DeliveryResult dr = future.get();
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
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    
    
    @Test
    public void testRejectData() throws Exception {
        int port = 6028;

        NettyServer smtpServer = create(new SimpleHook() {

            @Override
            public HookResult onMessage(SMTPSession session, MailEnvelope mail) {
                return new HookResult(HookReturnCode.DENY);
            }


        });
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();


       
        
        NettySMTPClientTransport transport = createSMTPClient();
        SMTPClientImpl c = new SMTPClientImpl(transport);

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes())), conf);
            DeliveryResult dr = future.get();
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
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    
    
    
    @Test
    public void testRejectOneRecipient() throws Exception {
        int port = 6028;


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


       
        
        NettySMTPClientTransport transport = createSMTPClient();
        SMTPClientImpl c = new SMTPClientImpl(transport);

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com", "to3@example.com"}), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes())), conf);
            DeliveryResult dr = future.get();
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
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
        
    }
    
    
    @Test
    public void testConnectionRefused() throws InterruptedException, ExecutionException {
        
        NettySMTPClientTransport transport = createSMTPClient();
        SMTPClientImpl c = new SMTPClientImpl(transport);

        SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
        conf.setConnectionTimeout(4);
        conf.setResponseTimeout(5);
        SMTPClientFuture future = c.deliver(new InetSocketAddress(11111), "from@example.com", Arrays.asList(new String[] { "to@example.com" }), new SimpleMessageInput(new ByteArrayInputStream("msg".getBytes())), conf);
        try {
            DeliveryResult dr = future.get();
            assertFalse(dr.isSuccess());
            assertNull(dr.getRecipientStatus());
            assertEquals(SMTPConnectionException.class, dr.getException().getClass());
        } finally {
            transport.destroy();
        }
    }
    
   

}
