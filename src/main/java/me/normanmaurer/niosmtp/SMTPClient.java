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
package me.normanmaurer.niosmtp;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * A client which deliver email via SMTP
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClient {

    /**
     * Deliver an email to the given {@link Collection} of recipients. The {@link InputStream} must be a valid email (valid encoding).
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
    public SMTPClientFuture deliver(InetSocketAddress host, String mailFrom, Collection<String> recipients, InputStream msg, SMTPClientConfig config);
}
