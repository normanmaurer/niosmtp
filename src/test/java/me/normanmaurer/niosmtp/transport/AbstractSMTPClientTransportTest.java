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
package me.normanmaurer.niosmtp.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.impl.SMTPClientConfigImpl;
import me.normanmaurer.niosmtp.util.TestUtils;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.hook.Hook;
import org.apache.james.protocols.smtp.hook.SimpleHook;
import org.junit.Test;

public abstract class AbstractSMTPClientTransportTest {

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
    public void testConnect() throws Exception {
        int port = TestUtils.getFreePort();

        NettyServer smtpServer = create(new SimpleHook());
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();

        SMTPClientTransport transport = createSMTPClient();
        try {
            SMTPClientConfigImpl conf = createConfig();

            final CountDownLatch latch = new CountDownLatch(1);
            transport.connect(new InetSocketAddress(port), conf, new CountingSMTPResponseCallback(latch, new ClosingSMTPResponseCallback(new SuccessSMTPResponseCallback(220, 1))));
            latch.await();
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
    }
    
    

    @Test
    public void testWriteSMTPRequest() throws Exception {
        int port = TestUtils.getFreePort();

        NettyServer smtpServer = create(new SimpleHook());
        smtpServer.setListenAddresses(Arrays.asList(new InetSocketAddress(port)));

        smtpServer.bind();

        SMTPClientTransport transport = createSMTPClient();
        try {
            SMTPClientConfigImpl conf = createConfig();

            final CountDownLatch latch = new CountDownLatch(2);
            transport.connect(new InetSocketAddress(port), conf, new CountingSMTPResponseCallback(latch, new SuccessSMTPResponseCallback(220, 1)) {

                @Override
                public void onResponse(SMTPClientSession session, SMTPResponse response) {
                    try {
                        super.onResponse(session, response);
                        session.send(SMTPRequestImpl.ehlo("localhost"), new CountingSMTPResponseCallback(latch, new ClosingSMTPResponseCallback(new SuccessSMTPResponseCallback(250, 4))));
                    } catch (Exception e) {
                        latch.countDown();
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onException(SMTPClientSession session, Throwable t) {
                    try {
                        super.onException(session, t);
                    } finally {
                        latch.countDown();
                    }
                }
                
            });
            latch.await();
        } finally {
            smtpServer.unbind();
            transport.destroy();
        }
    }
    

    @Test
    public void testConnectRefused() throws Exception {
        int port = TestUtils.getFreePort();

        SMTPClientTransport transport = createSMTPClient();
        try {
            SMTPClientConfigImpl conf = createConfig();

            final CountDownLatch latch = new CountDownLatch(1);
            transport.connect(new InetSocketAddress(port), conf, new CountingSMTPResponseCallback(latch, new SMTPResponseCallback() {
                
                @Override
                public void onResponse(SMTPClientSession session, SMTPResponse response) {
                    session.close();
                    fail();
                }
                
                @Override
                public void onException(SMTPClientSession session, Throwable t) {
                    session.close();
                    assertNotNull(t);
                }
            }));
            latch.await();
        } finally {
            transport.destroy();
        }
    }
    
    
    protected class ClosingSMTPResponseCallback implements SMTPResponseCallback {

        private SMTPResponseCallback callback;

        public ClosingSMTPResponseCallback(SMTPResponseCallback callback) {
            this.callback = callback;
        }
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            try {
                callback.onResponse(session, response);
            } finally {
                session.close();
            }
        }

        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            try {
                callback.onException(session, t);
            } finally {
                session.close();
            }
        }
        
    }
    protected class SuccessSMTPResponseCallback implements SMTPResponseCallback {
        
        private int code;
        private int linesCount;

        public SuccessSMTPResponseCallback(int code, int linesCount) {
            this.code = code;
            this.linesCount = linesCount;
        }
        
        
        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            fail();
        }

        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            assertEquals(code, response.getCode());
            assertEquals(linesCount, response.getLines().size());            
        }
    }
    
    protected class CountingSMTPResponseCallback implements SMTPResponseCallback {

        private final SMTPResponseCallback callback;
        private final CountDownLatch latch;

        public CountingSMTPResponseCallback(CountDownLatch latch, SMTPResponseCallback callback) {
            this.callback = callback;
            this.latch = latch;
        }

        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            try {
                callback.onResponse(session, response);
            } finally {
                latch.countDown();
            }
        }

        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            try {
                callback.onException(session, t);
            } finally {
                latch.countDown();
            }
        }
    }

}
