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

    public static final int DEFAULT_CONNECTION_TIMEOUT = 60;
    public static final String DEFAULT_HELO_NAME = "localhost";
    public static final int DEFAULT_RESPONSE_TIMEOUT = 60;
    
    private String heloName = DEFAULT_HELO_NAME;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private InetSocketAddress localAddress = null;
    private PipeliningMode pipeliningMode = PipeliningMode.TRY;;
    private int responseTimeout;
    
    public SMTPClientConfigImpl() {
    }
    
    /**
     * Set the name which will be used for EHLO/HELO. Default is {@link #DEFAULT_HELO_NAME}
     * 
     * @param heloName
     */
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

    /**
     * Set the connection timeout in seconds to use. Default is {@link #DEFAULT_CONNECTION_TIMEOUT}
     * 
     * @param connectionTimeout
     */
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
    
    /**
     * Set the local address to which the SMTPClient should get bound. Default is auto-detect which is done by setting this
     * to <code>null</code>
     * 
     * @param localAddress
     */
    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#usePipelining()
     */
    public PipeliningMode getPipeliningMode() {
        return pipeliningMode;
    }
    
    /**
     * Specify if <code>PIPELINING</code> should get used if possible. Default is {@link PipeliningMode#TRY}
     * 
     * @param pipeliningMode
     */
    public void setPipeliningMode(PipeliningMode pipeliningMode) {
        this.pipeliningMode = pipeliningMode;
    }


    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getResponseTimeout()
     */
    public int getResponseTimeout() {
        return responseTimeout;
    }
    
    /**
     * Set the response timeout to use. Default is {@link #DEFAULT_RESPONSE_TIMEOUT}
     * 
     * @param responseTimeout
     */
    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

}
