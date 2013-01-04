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
package me.normanmaurer.niosmtp.transport;

import javax.net.ssl.SSLContext;

import me.normanmaurer.niosmtp.SMTPUnsupportedExtensionException;

/**
 * Factory which is responsible for creating {@link SMTPClientTransport} implementations
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientTransportFactory {

    /**
     * Create a {@link SMTPClientTransport} instance which use plain SMTP 
     * 
     * @return plainClient
     */
    SMTPClientTransport createPlain();
    
    /**
     * Return a {@link SMTPClientTransport} which use SMTPS (encrypted)
     * 
     * @param context
     * @return smtpsClient
     */
    SMTPClientTransport createSMTPS(SSLContext context);
    
    /**
     * Create {@link SMTPClientTransport} which uses plain SMTP but switch to encryption later via the STARTTLS extension
     * 
     * @param context
     * @param failOnNoSupport if true the client will throw an {@link SMTPUnsupportedExtensionException} if STARTTLS is not supported.
     *                        If false it will just continue in plain mode (no encryption)
     * @return starttlsClient
     */
    SMTPClientTransport createStartTLS(SSLContext context, boolean failOnNoSupport);
}