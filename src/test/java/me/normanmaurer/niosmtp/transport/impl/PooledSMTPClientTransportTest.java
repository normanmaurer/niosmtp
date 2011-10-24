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
import java.util.List;
import java.util.concurrent.CountDownLatch;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.util.TestUtils;

import org.junit.Test;

public class PooledSMTPClientTransportTest {
    
    @Test
    public void testPooling() throws InterruptedException {
        int port = TestUtils.getFreePort();

        TestResponseCallback callback = new TestResponseCallback(3);
        PooledSMTPClientTransport transport = new PooledSMTPClientTransport(new MockSMTPClientTransport(), 30, 30);
        transport.connect(new InetSocketAddress("127.0.0.1", port), new SMTPClientConfigImpl(), callback);
        transport.connect(new InetSocketAddress("127.0.0.1", port), new SMTPClientConfigImpl(), new ClosingResponseCallback(callback));
        transport.connect(new InetSocketAddress("127.0.0.1", port), new SMTPClientConfigImpl(), callback);

        List<SMTPClientSession> session = callback.awaitSessions();
        assertEquals(3, session.size());
        assertFalse(session.get(0).getId().equals(session.get(1).getId()));

        assertEquals(session.get(1).getId(), session.get(2).getId());

        transport.destroy();
        
    }
    
    private final class TestResponseCallback implements SMTPResponseCallback {
        private final CountDownLatch latch;
        public TestResponseCallback(int count) {
            latch = new CountDownLatch(count);
        }

        public List<SMTPClientSession> sessions = new ArrayList<SMTPClientSession>();
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            sessions.add(session);
            latch.countDown();
        }

        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            fail();
        }
        
        
        public List<SMTPClientSession> awaitSessions() throws InterruptedException {
            latch.await();
            return sessions;
        }
    }

    private final class ClosingResponseCallback implements SMTPResponseCallback {

        private SMTPResponseCallback wrapped;

        public ClosingResponseCallback(SMTPResponseCallback wrapped) {
            this.wrapped = wrapped;
        }
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            wrapped.onResponse(session,response);
            session.close();
        }

        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            wrapped.onException(session, t);
            session.close();
        }
        
    }
}
