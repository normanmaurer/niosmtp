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

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Configuration which is used to deliver email via SMTP
 *
 * @author Norman Maurer
 *
 */
public interface SMTPClientConfig {

    
    /**
     * Return the name which will get used for the HELO/EHLO
     * 
     * @return name
     */
    public String getHeloName();
    
    /**
     * Return the connection timeout (in seconds) for the client
     * 
     * @return connectionTimeout
     */
    public int getConnectionTimeout();
    
    /**
     * Return the response timeout (in seconds) for the SMTP Server to send the response
     * 
     * @return responseTimeout
     */
    public int getResponseTimeout();

    
    /**
     * Return the {@link InetAddress} which should get used to bind to or null if no specific should get used
     * 
     * @return local
     */
    public InetSocketAddress getLocalAddress();
 
    /**
     * Return <code>true</code> if the client should use PIPELINING if possible
     * 
     * @return pipelining
     */
    public boolean usePipelining();
}
