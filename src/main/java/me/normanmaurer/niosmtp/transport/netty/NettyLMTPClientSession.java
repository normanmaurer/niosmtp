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
package me.normanmaurer.niosmtp.transport.netty;

import java.util.Collection;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPMessageSubmit;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;

/**
 * 
 * @author Norman Maurer
 *
 */
public class NettyLMTPClientSession extends NettySMTPClientSession{

    public NettyLMTPClientSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine) {
        super(channel, logger, config, mode, engine);
    }

    @Override
    public SMTPClientFuture<FutureResult<Collection<SMTPResponse>>> send(SMTPMessageSubmit msg) {
        SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>> future = new SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>>(false);
        future.setSMTPClientSession(this);

        addCollectionFutureHandler(future, msg.getRecipients());
        writeMessage(msg.getMessage());
        return future;
    }

    
}
