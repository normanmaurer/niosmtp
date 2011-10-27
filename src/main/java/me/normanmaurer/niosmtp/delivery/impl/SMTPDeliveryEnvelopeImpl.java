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

import java.util.Collection;
import java.util.Collections;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryEnvelope;

/**
 * Simple pojo implementation of a {@link SMTPDeliveryEnvelope}
 * 
 * @author Norman Maurer
 *
 */
public class SMTPDeliveryEnvelopeImpl implements SMTPDeliveryEnvelope{

    private final Collection<String> recipients;
    private final String sender;
    private final MessageInput message;

    public SMTPDeliveryEnvelopeImpl(final String sender, final Collection<String> recipients, final MessageInput message) {
        this(sender, recipients, message, true);
    }
    
    private SMTPDeliveryEnvelopeImpl(final String sender, final Collection<String> recipients, final MessageInput message, boolean wrapRecipients) {
        this.sender = sender;
        if (wrapRecipients) {
            this.recipients = Collections.unmodifiableCollection(recipients);
        } else {
            this.recipients = recipients;
        }
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient must be given");
        }
        this.message = message;
    }
    
    
    
    /**
     * Construct a {@link SMTPDeliveryEnvelope} which will use a null-sender
     * 
     * @param recipients
     * @param message
     */
    public SMTPDeliveryEnvelopeImpl( final Collection<String> recipients, final MessageInput message) {
        this(null, recipients, message);
    }
    
    
    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public Collection<String> getRecipients() {
        return recipients;
    }

    @Override
    public MessageInput getMessage() {
        return message;
    }
    
  
    
    /**
     * Create an array of {@link SMTPDeliveryEnvelope}'s which use the same sender and recipients but different {@link MessageInput}'s
     * 
     * @param sender
     * @param recipients
     * @param messages
     * @return transactions
     */
    public static SMTPDeliveryEnvelope[] create(final String sender, final Collection<String> recipients, final MessageInput... messages) {
        if (messages == null || messages.length <1 ){
            throw new IllegalArgumentException("At least one MessageInput must be given");
        }
        
        Collection<String> rcpts = Collections.unmodifiableCollection(recipients);
        SMTPDeliveryEnvelope[] transactions = new SMTPDeliveryEnvelope[messages.length];
        for (int i = 0; i < transactions.length; i++) {
            transactions[i] = new SMTPDeliveryEnvelopeImpl(sender, rcpts , messages[i], false);
        }
        return transactions;
    }

}
