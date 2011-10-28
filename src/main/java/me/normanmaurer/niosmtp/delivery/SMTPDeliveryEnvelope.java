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

import java.util.Collection;

import me.normanmaurer.niosmtp.SMTPMessage;

/**
 * A {@link SMTPDeliveryEnvelope} is a complete transaction which is used to deliver an email per SMTP. 
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPDeliveryEnvelope {

    /**
     * Return the sender which should be used for the <code>MAIL FROM</code>. This may return <code>null</code> if a null-sender should be used
     * 
     * @return sender
     */
    String getSender();

    /**
     * Return a {@link Collection} of recipients which should be used for the <code>RCPT TO</code>. The returned {@link Collection} must at least contain one element.
     * <br/>
     * <br/>
     * <strong>The returned {@link Collection} is unmodifiable</strong>
     * 
     * @return recipients not null
     */
    Collection<String> getRecipients();
    
    /**
     * Return the {@link SMTPMessage} which should be used to submit the message after the <code>DATA</code> command was issued
     * 
     * @return messageInput not null
     */
    SMTPMessage getMessage();
}
