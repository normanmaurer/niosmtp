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
package me.normanmaurer.niosmtp.impl;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;

import me.normanmaurer.niosmtp.SMTPClient;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SecureMode;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.impl.internal.SecureSMTPClientPipelineFactory;

import org.jboss.netty.channel.ChannelPipelineFactory;

/**
 * {@link SMTPClient} implementation which will create a new Connection for
 * every {@link #deliver(InetSocketAddress, String, List, InputStream, SMTPClientConfig)}
 * call and use SMTPS for the delivery
 * 
 * So no pooling is active
 * 
 * @author Norman Maurer
 * 
 */
public class UnpooledSecureSMTPClient extends UnpooledSMTPClient{

    private final SSLContext context;
    private final SecureMode secureMode;
    


    public UnpooledSecureSMTPClient(SSLContext context, SecureMode secureMode) {
        this.context = context;
        this.secureMode = secureMode;
    }

    public UnpooledSecureSMTPClient(SSLContext context) {
        this(context, SecureMode.SMTPS);
    }
    @Override
    protected ChannelPipelineFactory createChannelPipelineFactory(SMTPClientFutureImpl future, String mailFrom, LinkedList<String> recipients, InputStream msg, SMTPClientConfig config) {
        return new SecureSMTPClientPipelineFactory(future, mailFrom, recipients, msg, config, timer, secureMode, context);
    }


}
