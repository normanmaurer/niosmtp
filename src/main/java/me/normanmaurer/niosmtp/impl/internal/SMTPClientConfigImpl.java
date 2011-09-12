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
package me.normanmaurer.niosmtp.impl.internal;

import java.net.InetSocketAddress;

import me.normanmaurer.niosmtp.SMTPClientConfig;

/**
 * Simple {@link SMTPClientConfig} implementation which allows
 * to handle the config via a POJO.
 * 
 * @author Norman Maurer
 *
 */
public class SMTPClientConfigImpl implements SMTPClientConfig {

    private String heloName = "localhost";
    private int connectionTimeout = 60;
    private InetSocketAddress localAddress = null;
    private boolean usePipelining = true;
    private int responseTimeout;
    
    public SMTPClientConfigImpl() {
    }
    
    public void setHeloName(String heloName) {
        this.heloName = heloName;
    }
    
    
    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getHeloName()
     */
    public String getHeloName() {
        return heloName;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getConnectionTimeout()
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    

    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getLocalAddress()
     */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }
    
    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#usePipelining()
     */
    public boolean usePipelining() {
        return usePipelining;
    }
    
    public void setUsePipelining(boolean usePipelining) {
        this.usePipelining = usePipelining;
    }


    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getResponseTimeout()
     */
    public int getResponseTimeout() {
        return responseTimeout;
    }
    
    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

}
