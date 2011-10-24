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
package me.normanmaurer.niosmtp.delivery;

import me.normanmaurer.niosmtp.Authentication;
import me.normanmaurer.niosmtp.SMTPClientConfig;

/**
 * Configuration which is used to deliver email via SMTP
 *
 * @author Norman Maurer
 *
 */
public interface SMTPDeliveryAgentConfig extends SMTPClientConfig {

    public enum PipeliningMode {

        /**
         * 
         * Don't use PIPELINING
         */
        NO,

        /**
         * 
         * Use PIPELINING if the server supports it
         */
        TRY,

        /**
         * 
         * Use PIPELINING if the server supports it otherwise fail
         */
        DEPEND

    }

    /**
     * 
     * Return the name which will get used for the HELO/EHLO
     * 
     * @return name
     */

    String getHeloName();


    /**
     * 
     * Return {@link PipeliningMode} if the client should use PIPELINING if
     * possible
     * 
     * @return pipelining
     */
    PipeliningMode getPipeliningMode();

    /**
     * 
     * Return the {@link Authentication} to use or <code>null</code> if none
     * should be used
     * 
     * @return auth
     */
    Authentication getAuthentication();

}
