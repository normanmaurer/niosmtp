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

import java.net.InetSocketAddress;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

public class MockSMTPClientTransport implements SMTPClientTransport {
    
    @Override
    public SMTPDeliveryMode getDeliveryMode() {
        return SMTPDeliveryMode.PLAIN;
    }
    
    @Override
    public void destroy() {
        // Do nothing
        
    }
    
    @Override
    public SMTPClientFuture<FutureResult<SMTPResponse>> connect(InetSocketAddress remote, SMTPClientConfig config) {
        SMTPClientSession session = new MockSMTPClientSession(config);

        return null;
    }
}
