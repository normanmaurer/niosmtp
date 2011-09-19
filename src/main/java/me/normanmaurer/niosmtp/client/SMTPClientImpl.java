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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientConstants;
import me.normanmaurer.niosmtp.client.callback.WelcomeResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.impl.internal.NettySMTPClientFuture;



/**
 * {@link SMTPClientImpl} which use the wrapped {@link SMTPClientTransport} to deliver email
 * 
 * So no pooling is active
 * 
 * @author Norman Maurer
 * 
 */
public class SMTPClientImpl implements SMTPClientConstants,SMTPClient {

    private SMTPClientTransport transport;

    public SMTPClientImpl(SMTPClientTransport transport) {
        this.transport = transport;
    }

    
    


    @Override
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final Collection<String> recipients, final MessageInput msg, final SMTPClientConfig config) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient must be given");
        }
        
        LinkedList<String> rcpts = new LinkedList<String>(recipients);
        final NettySMTPClientFuture future = new NettySMTPClientFuture();

        transport.connect(host, config,new WelcomeResponseCallback(future, mailFrom, rcpts, msg, config));
        
       
        return future;
    }
}
