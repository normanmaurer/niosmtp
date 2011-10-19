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
package me.normanmaurer.niosmtp.client;

import java.util.Collection;

import me.normanmaurer.niosmtp.MessageInput;

/**
 * Simple pojo implementation of a {@link SMTPTransaction}
 * 
 * @author Norman Maurer
 *
 */
public class SMTPTransactionImpl implements SMTPTransaction{

    private final Collection<String> recipients;
    private final String sender;
    private final MessageInput message;

    public SMTPTransactionImpl(final String sender, final Collection<String> recipients, final MessageInput message) {
        this.sender = sender;
        this.recipients = recipients;
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient must be given");
        }
        this.message = message;
    }
    
    
    /**
     * Construct a {@link SMTPTransaction} which will use a null-sender
     * 
     * @param recipients
     * @param message
     */
    public SMTPTransactionImpl( final Collection<String> recipients, final MessageInput message) {
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
     * Create an array of {@link SMTPTransaction}'s which use the same sender and recipients but different {@link MessageInput}'s
     * 
     * @param sender
     * @param recipients
     * @param messages
     * @return transactions
     */
    public static SMTPTransaction[] create(final String sender, final Collection<String> recipients, final MessageInput... messages) {
        if (messages == null || messages.length <1 ){
            throw new IllegalArgumentException("At least one MessageInput must be given");
        }
        SMTPTransaction[] transactions = new SMTPTransaction[messages.length];
        for (int i = 0; i < transactions.length; i++) {
            transactions[i] = new SMTPTransactionImpl(sender, recipients, messages[i]);
        }
        return transactions;
    }

}
