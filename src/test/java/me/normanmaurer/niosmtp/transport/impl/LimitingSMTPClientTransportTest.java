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
package me.normanmaurer.niosmtp.transport.impl;

import static org.junit.Assert.*;


import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPResponseImpl;
import me.normanmaurer.niosmtp.transport.AbstractSMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

public class LimitingSMTPClientTransportTest {

    private final class MockSMTPClientSession extends AbstractSMTPClientSession {
        
        public MockSMTPClientSession() {
            super(LoggerFactory.getLogger(MockSMTPClientSession.class), new SMTPClientConfigImpl(), SMTPDeliveryMode.PLAIN, null, null);
        }

        private final List<CloseListener> cListeners = new ArrayList<CloseListener>();
        private boolean closed = false;
        @Override
        public void startTLS() {
            // do nothing
        }
        
        @Override
        public void send(MessageInput request, SMTPResponseCallback callback) {
            callback.onResponse(this, new SMTPResponseImpl(250));
            
        }
        
        @Override
        public void send(SMTPRequest request, SMTPResponseCallback callback) {
            callback.onResponse(this, new SMTPResponseImpl(250));
        }
        
        @Override
        public boolean isEncrypted() {
            return false;
        }
        
        @Override
        public synchronized boolean isClosed() {
            return closed;
        }
        
        @Override
        public String getId() {
            return "";
        }
        
        @Override
        public synchronized void close() {
            closed = true;
            for (CloseListener l: cListeners) {
                l.onClose(this);
            }
        }
        
        @Override
        public synchronized void addCloseListener(CloseListener listener) {
            cListeners.add(listener);
        }

        @Override
        public synchronized void removeCloseListener(CloseListener listener) {
            cListeners.remove(listener);
        }

        @Override
        public synchronized Iterator<CloseListener> getCloseListeners() {
            return new ArrayList<CloseListener>(cListeners).iterator();
        }
    }
    
    private final class MockSMTPClientTransport implements SMTPClientTransport {
        
        @Override
        public SMTPDeliveryMode getDeliveryMode() {
            return SMTPDeliveryMode.PLAIN;
        }
        
        @Override
        public void destroy() {
            // Do nothing
            
        }
        
        @Override
        public void connect(InetSocketAddress remote, SMTPClientConfig config, SMTPResponseCallback callback) {
            callback.onResponse(new MockSMTPClientSession(), new SMTPResponseImpl(220));
        }
    }
    
    @Test
    public void testLimitWithNoQueue() throws InterruptedException {
        LimitingSMTPClientTransport transport = new LimitingSMTPClientTransport(new MockSMTPClientTransport(), 1, 0);
        
        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean success = new AtomicBoolean(false);

        transport.connect(new InetSocketAddress(1), new SMTPClientConfigImpl(), new SMTPResponseCallback() {
            
            @Override
            public void onResponse(SMTPClientSession session, SMTPResponse response) {
                try {
                    assertEquals(1, count.incrementAndGet());
                    success.set(true);
                } finally {
                    latch.countDown(); 
                }
            }
            
            @Override
            public void onException(SMTPClientSession session, Throwable t) {
                try {
                    fail();
                } finally {
                    latch.countDown();  
                }
            }
        });

        final AtomicBoolean failed = new AtomicBoolean(false);
        transport.connect(new InetSocketAddress(1), new SMTPClientConfigImpl(), new SMTPResponseCallback() {
            
            @Override
            public void onResponse(SMTPClientSession session, SMTPResponse response) {
                try {
                    fail();
                } finally {
                    latch.countDown(); 
                }
            }
            
            @Override
            public void onException(SMTPClientSession session, Throwable t) {
                try {
                    failed.set(true);
                } finally {
                    latch.countDown();  
                }
            }
        });
        latch.await();  
        assertEquals(1, count.get());
        assertTrue(success.get());
        assertTrue(failed.get());
    }
    
    @Test
    public void testLimitWithQueue() throws InterruptedException {
        LimitingSMTPClientTransport transport = new LimitingSMTPClientTransport(new MockSMTPClientTransport(), 1, 1);
        
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(2);

        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean success = new AtomicBoolean(false);
        final AtomicBoolean success2 = new AtomicBoolean(false);

        final AtomicReference<SMTPClientSession> lastsession = new AtomicReference<SMTPClientSession>();
        
        transport.connect(new InetSocketAddress(1), new SMTPClientConfigImpl(), new SMTPResponseCallback() {
            
            @Override
            public void onResponse(SMTPClientSession session, SMTPResponse response) {
                try {
                    assertEquals(1, count.incrementAndGet());
                    success.set(true);
                    lastsession.set(session);
                } finally {
                    latch.countDown(); 
                    latch2.countDown(); 
                }
            }
            
            @Override
            public void onException(SMTPClientSession session, Throwable t) {
                try {
                    fail();
                } finally {
                    latch.countDown();  
                    latch2.countDown(); 
                }
            }
        });

        transport.connect(new InetSocketAddress(1), new SMTPClientConfigImpl(), new SMTPResponseCallback() {
            
            @Override
            public void onResponse(SMTPClientSession session, SMTPResponse response) {
                try {
                    assertEquals(2, count.incrementAndGet());
                    success2.set(true);
                } finally {
                    latch2.countDown(); 
                }
            }
            
            @Override
            public void onException(SMTPClientSession session, Throwable t) {
                try {
                    fail();
                } finally {
                    latch2.countDown(); 
                }
            }
        });
        latch.await();  
        assertEquals(1, count.get());
        assertTrue(success.get());
        assertFalse(success2.get());
        
        lastsession.get().close();
        
        latch2.await();  
        assertEquals(2, count.get());
        assertTrue(success.get());
        assertTrue(success2.get());
    }
}
