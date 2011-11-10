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

/**
 * LMTP-Servers MUST support <code>PIPELINING</code> according the RFC. So this
 * configuration force them todo so by return {@link PipeliningMode#DEPEND} via the
 * {@link #getPipeliningMode()} method. 
 * 
 * @author Maurer
 *
 */
public class StrictLMTPDeliveryAgentConfig extends SMTPDeliveryAgentConfigImpl{

    /**
     * Returns {@link PipeliningMode#DEPEND}
     */
    @Override
    public PipeliningMode getPipeliningMode() {
        return PipeliningMode.DEPEND;
    }

    /**
     * Throws an {@link IllegalArgumentException} if set it to something
     * different then {@link PipeliningMode#DEPEND}
     */
    @Override
    public void setPipeliningMode(PipeliningMode pipeliningMode) {
        if (pipeliningMode != PipeliningMode.DEPEND) {
            throw new IllegalArgumentException("LMTP-Servers MUST implement PIPELINING");
        }
    }

}
