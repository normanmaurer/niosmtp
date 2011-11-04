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
package me.normanmaurer.niosmtp.delivery.impl;

import java.net.InetSocketAddress;

import me.normanmaurer.niosmtp.delivery.Authentication;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryAgentConfig;
import me.normanmaurer.niosmtp.transport.impl.SMTPClientConfigImpl;

/**
 * 
 * @author Norman Maurer
 *
 */
public class SMTPDeliveryAgentConfigImpl extends SMTPClientConfigImpl implements SMTPDeliveryAgentConfig {

    private String heloName = DEFAULT_HELO_NAME;

    private PipeliningMode pipeliningMode = PipeliningMode.TRY;;

    private Authentication auth;

    /**
     * 
     * Set the name which will be used for EHLO/HELO. Default is
     * {@link #DEFAULT_HELO_NAME}
     * 
     * - *
     * 
     * @param heloName
     */
    public void setHeloName(String heloName) {
        this.heloName = heloName;
    }

    @Override
    public String getHeloName() {
        return heloName;
    }

    @Override
    public PipeliningMode getPipeliningMode() {
        return pipeliningMode;
    }

    /**
     * 
     * Specify if <code>PIPELINING</code> should get used if possible. Default
     * is {@link PipeliningMode#TRY}
     * 
     * 
     * 
     * @param pipeliningMode
     */
    public void setPipeliningMode(PipeliningMode pipeliningMode) {
        this.pipeliningMode = pipeliningMode;
    }

    @Override
    public Authentication getAuthentication() {
        return auth;
    }

    /**
     * 
     * Set the {@link Authentication} to use. If you don't want to use AUTH just
     * use <code>null</code> as parameter.
     * 
     * Default is <code>null</code>
     * 
     * 
     * 
     * @param auth
     */
    public void setAuthentication(Authentication auth) {
        this.auth = auth;
    }
    
    /**
     * Return a immutable copy of this {@link SMTPDeliveryAgentConfigImpl}
     * 
     * @return immutable
     */
    public SMTPDeliveryAgentConfig immutable() {
        return new ImmutableSMTPDeliveryAgentConfig(this);
    }
    
    private final static class ImmutableSMTPDeliveryAgentConfig implements SMTPDeliveryAgentConfig {
        private final SMTPDeliveryAgentConfig config;
        
        public ImmutableSMTPDeliveryAgentConfig(SMTPDeliveryAgentConfig config) {
            this.config = config;
        }
        @Override
        public int getConnectionTimeout() {
            return config.getConnectionTimeout();
        }

        @Override
        public int getResponseTimeout() {
            return config.getResponseTimeout();
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return config.getLocalAddress();
        }

        @Override
        public String getHeloName() {
            return config.getHeloName();
        }

        @Override
        public PipeliningMode getPipeliningMode() {
            return config.getPipeliningMode();
        }

        @Override
        public Authentication getAuthentication() {
            return config.getAuthentication();
        }
        
    }

}