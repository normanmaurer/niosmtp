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

import me.normanmaurer.niosmtp.SMTPClientConfig;

/**
 * SMTP Client which allows to deliver email to an SMTP Server in a async manner
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClient {

    /**
     * Deliver the given {@link SMTPTransaction}'s 
     * 
     * The implementation may choose to do the deliver in an async fashion. 
     * 
     * @param host
     * @param config
     * @param transation
     * @return future
     */
    public SMTPClientFuture deliver(InetSocketAddress host, final SMTPClientConfig config, final SMTPTransaction... transaction);

}
