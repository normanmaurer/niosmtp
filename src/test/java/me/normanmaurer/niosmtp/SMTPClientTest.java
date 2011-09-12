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
import org.subethamail.smtp.server.SMTPServer;

public class SMTPClientTest {
    

    @Test
    public void testRejectMailFrom() throws InterruptedException, ExecutionException {
        int port = 6028;

        SMTPServer smtpServer = new SMTPServer(new TestHandlerFactory() {

            @Override
            public void from(String sender) throws RejectException {
                throw new RejectException("Sender " + sender + " rejected");
            }
            
        });
        smtpServer.setPort(port);
        smtpServer.start();

       
        UnpooledSMTPClient c = new UnpooledSMTPClient();

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            System.out.println("delivering...");
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

        SMTPServer smtpServer = new SMTPServer(new TestHandlerFactory() {

            @Override
            public void recipient(String rcpt) throws RejectException {
                throw new RejectException();
            }


            
        });
        smtpServer.setPort(port);
        smtpServer.start();

       
        UnpooledSMTPClient c = new UnpooledSMTPClient();

        try {
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            System.out.println("delivering...");
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
    public void testConnectionRefused() throws InterruptedException, ExecutionException {
        UnpooledSMTPClient c = new UnpooledSMTPClient();
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
