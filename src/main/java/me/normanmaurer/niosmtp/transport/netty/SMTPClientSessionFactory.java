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

import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;

import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

/**
 * Factory which is responsible for creating {@link SMTPClientSession}'s
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientSessionFactory {

    /**
     * Create a new {@link SMTPClientSession}
     * 
     * @param channel
     * @param logger
     * @param config
     * @param mode
     * @param engine
     * @return session
     */
    SMTPClientSession newSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine);
}
