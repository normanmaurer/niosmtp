/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* Selene licenses this file to You under the Apache License, Version 2.0
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
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


import me.normanmaurer.niosmtp.impl.UnpooledSMTPClient;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientConfigImpl;

import org.junit.Test;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.command.EhloCommand;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.Session;

public class SMTPClientTest {
    

    protected SMTPServer create(MessageHandlerFactory factory) {
        return new SMTPServer(factory);
        
    }
    
    protected UnpooledSMTPClient createSMTPClient() {
        return new UnpooledSMTPClient();
    }
    
    @Test
    public void testRejectMailFrom() throws InterruptedException, ExecutionException {
        int port = 6028;

        SMTPServer smtpServer = create(new TestHandlerFactory() {

            @Override
            public void from(String sender) throws RejectException {
                throw new RejectException("Sender " + sender + " rejected");
            }
            
        });
        smtpServer.setPort(port);

        smtpServer.start();

       
        UnpooledSMTPClient c = createSMTPClient();

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new ByteArrayInputStream("msg".getBytes()), conf);
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
            smtpServer.stop();
            c.destroy();
        }
        
    }


    @Test
    public void testRejectHelo() throws InterruptedException, ExecutionException {
        int port = 6028;

        SMTPServer smtpServer = create(new TestHandlerFactory());
        
        // Reject on EHLO
        smtpServer.getCommandHandler().addCommand(new EhloCommand() {

            @Override
            public void execute(String commandString, Session sess) throws IOException {
                String[] args = this.getArgs(commandString);

                sess.setHelo(args[1]);
                sess.sendResponse("554 " + sess.getServer().getHostName() + " rejected");

            }
            
        });
        smtpServer.setPort(port);

        smtpServer.start();

       
        UnpooledSMTPClient c = createSMTPClient();

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new ByteArrayInputStream("msg".getBytes()), conf);
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
            smtpServer.stop();
            c.destroy();
        }
        
    }
    @Test
    public void testRejectAllRecipients() throws InterruptedException, ExecutionException {
        int port = 6028;

        SMTPServer smtpServer = create(new TestHandlerFactory() {

            @Override
            public void recipient(String rcpt) throws RejectException {
                throw new RejectException();
            }


            
        });
        smtpServer.setPort(port);

        smtpServer.start();

       
        UnpooledSMTPClient c = createSMTPClient();

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new ByteArrayInputStream("msg".getBytes()), conf);
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
            smtpServer.stop();
            c.destroy();
        }
        
    }
    
    
    @Test
    public void testRejectData() throws InterruptedException, ExecutionException {
        int port = 6028;

        SMTPServer smtpServer = create(new TestHandlerFactory() {

            @Override
            public void data(InputStream arg0) throws RejectException, TooMuchDataException, IOException {
                throw new RejectException();
            }


            
        });
        smtpServer.setPort(port);

        smtpServer.start();

       
        UnpooledSMTPClient c = createSMTPClient();

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com"}), new ByteArrayInputStream("msg".getBytes()), conf);
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
            smtpServer.stop();
            c.destroy();
        }
        
    }
    
    
    
    @Test
    public void testRejectOneRecipient() throws InterruptedException, ExecutionException {
        int port = 6028;

        SMTPServer smtpServer = create(new TestHandlerFactory() {

            @Override
            public void recipient(String rcpt) throws RejectException {
                if (rcpt.equals("to2@example.com"))  {
                    throw new RejectException();
                }
            }


            
        });
        smtpServer.setPort(port);

        smtpServer.start();

       
        UnpooledSMTPClient c = createSMTPClient();

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            SMTPClientFuture future = c.deliver(new InetSocketAddress(port), "from@example.com", Arrays.asList(new String[] {"to@example.com", "to2@example.com", "to3@example.com"}), new ByteArrayInputStream("msg".getBytes()), conf);
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
            smtpServer.stop();
            c.destroy();
        }
        
    }
    
    
    @Test
    public void testConnectionRefused() throws InterruptedException, ExecutionException {
        UnpooledSMTPClient c = createSMTPClient();
        SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
        conf.setConnectionTimeout(4);
        conf.setResponseTimeout(5);
        SMTPClientFuture future = c.deliver(new InetSocketAddress(11111), "from@example.com", Arrays.asList(new String[] { "to@example.com" }), new ByteArrayInputStream("msg".getBytes()), conf);
        try {
            DeliveryResult dr = future.get();
            assertFalse(dr.isSuccess());
            assertNull(dr.getRecipientStatus());
            assertEquals(SMTPConnectionException.class, dr.getException().getClass());
        } finally {
            c.destroy();
        }
    }
    
    protected class TestHandlerFactory implements MessageHandlerFactory, MessageHandler {

        public TestHandlerFactory() {
            
        }
        @Override
        public void data(InputStream arg0) throws RejectException, TooMuchDataException, IOException {
            
        }

        @Override
        public void done() {
            
        }

        @Override
        public void from(String arg0) throws RejectException {
            
        }

        @Override
        public void recipient(String arg0) throws RejectException {
            
        }

        @Override
        public MessageHandler create(MessageContext arg0) {
            return this;
        }
        
    }

}
