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

import java.util.Collection;
import java.util.UUID;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPMessageSubmit;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.AbstractSMTPClientSession;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

import org.slf4j.LoggerFactory;

public class MockSMTPClientSession extends AbstractSMTPClientSession {
    private final String id = UUID.randomUUID().toString();
    
    public MockSMTPClientSession(SMTPClientConfig config) {
        super(LoggerFactory.getLogger(MockSMTPClientSession.class), config, SMTPDeliveryMode.PLAIN, null, null);
    }

    private boolean closed = false;
    @Override
    public SMTPClientFuture<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> startTLS() {
        return null;
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
        return id;
    }
  


    @Override
    public SMTPClientFuture<FutureResult<SMTPResponse>> send(SMTPRequest request) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public SMTPClientFuture<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> getCloseFuture() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public SMTPClientFuture<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> close() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public SMTPClientFuture<FutureResult<Collection<SMTPResponse>>> send(SMTPMessageSubmit request) {
        // TODO Auto-generated method stub
        return null;
    }
}