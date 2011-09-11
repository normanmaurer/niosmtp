package me.normanmaurer.niosmtp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import static org.mockito.Mockito.*;

import me.normanmaurer.niosmtp.impl.UnpooledSMTPClient;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientConfigImpl;

import org.apache.commons.configuration.Configuration;
import org.apache.james.dnsservice.api.DNSService;
import org.apache.james.dnsservice.api.TemporaryResolutionException;
import org.apache.james.filesystem.api.FileSystem;
import org.apache.james.protocols.api.ProtocolHandler;
import org.apache.james.protocols.api.ProtocolHandlerLoader;
import org.apache.james.smtpserver.netty.SMTPServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMTPClientTest {
    
    @Test
    public void test() throws InterruptedException, ExecutionException {
        SMTPServer s = new SMTPServer();
        DNSService dnsService = mock(DNSService.class);
        s.setDNSService(dnsService);
        ProtocolHandlerLoader phl = mock(ProtocolHandlerLoader.class);
        s.setProtocolHandlerLoader(phl);
        FileSystem fs = mock(FileSystem.class);
        s.setFileSystem(fs);
        InetSocketAddress address = new InetSocketAddress(6028);
        s.setListenAddresses(Arrays.asList(new InetSocketAddress[] { address }));
        Logger logger = LoggerFactory.getLogger("prova");
        s.setLog(logger);
        s.start();
        
        try {
            SMTPClient c = new UnpooledSMTPClient();
            SMTPClientConfigImpl conf = new SMTPClientConfigImpl();
            conf.setConnectionTimeout(4);
            conf.setResponseTimeout(5);
            System.out.println("delivering...");
            SMTPClientFuture future = c.deliver(address, "from@example.com", Arrays.asList(new String[] {"to@example.com"}), new ByteArrayInputStream("msg".getBytes()), conf);
            DeliveryResult dr = future.get();
            System.out.println(dr.isSuccess());
            System.out.println(dr.getRecipientStatus());
        } finally {
            s.stop();
        }
        
    }

}
