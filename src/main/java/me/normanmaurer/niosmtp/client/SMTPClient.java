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
package me.normanmaurer.niosmtp.client;

import java.net.InetSocketAddress;
import java.util.Collection;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;

/**
 * SMTP Client which allows to deliver email to an SMTP Server in a async manner
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClient {

    /**
     * Deliver an email to the given {@link Collection} of recipients. 
     * 
     * The delivery is done in an non-blocking fashion, so this method will return as fast as possible and then allow to get the result
     * via the {@link SMTPClientFuture}.
     * 
     * @param host
     * @param mailFrom
     * @param recipients
     * @param msg
     * @param config
     * @return future
     */
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final Collection<String> recipients, final MessageInput msg, final SMTPClientConfig config);

}
