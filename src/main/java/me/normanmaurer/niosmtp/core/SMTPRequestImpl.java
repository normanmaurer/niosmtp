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
package me.normanmaurer.niosmtp.core;

import me.normanmaurer.niosmtp.SMTPRequest;

public class SMTPRequestImpl implements SMTPRequest{

    private String command;
    private String argument;

    public SMTPRequestImpl(String command, String argument) {
        this.command = command;
        this.argument = argument;
    }
    
    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String getArgument() {
        return argument;
    }
    
    @Override
    public String toString() {
        return StringUtils.toString(this);
    }
    
    /**
     * Create <code>QUIT</code> {@link SMTPRequest}
     * 
     * @return quit
     */
    public static SMTPRequest quit() {
        return new SMTPRequestImpl("QUIT", null);
    }

    /**
     * Create a <code>HELO</code> {@link SMTPRequest}
     * 
     * 
     * @param heloName
     * @return helo
     */
    public static SMTPRequest helo(String heloName) {
        return new SMTPRequestImpl("HELO", heloName);
    }
    
    
    /**
     * Create a <code>EHLO</code> {@link SMTPRequest}
     * 
     * @param heloName
     * @return ehlo
     */
    public static SMTPRequest ehlo(String heloName) {
        return new SMTPRequestImpl("EHLO", heloName);
    }
    
    /**
     * Create a <code>RCPT</code> {@link SMTPRequest}
     * 
     * @param recipient
     * @return rcpt
     */
    public static SMTPRequest rcpt(String recipient) {
        return new SMTPRequestImpl("RCPT TO:", "<" + recipient + ">");
    }
    
    
    /**
     * Create a <code>MAIL</code> {@link SMTPRequest} 
     * 
     * @param sender
     * @return mail
     */
    public static SMTPRequest mail(String sender) {
        return new SMTPRequestImpl("MAIL FROM:",  "<" + sender + ">");
    }
    
    /**
     * Create a <code>DATA</code> {@link SMTPRequest}
     * 
     * @return data
     */
    public static SMTPRequest data() {
        return new SMTPRequestImpl("DATA", null);
    }
    
    /**
     * Create a <code>STARTTLS</code> {@link SMTPRequest}
     * 
     * @return starttls
     */
    public static SMTPRequest startTls() {
        return new SMTPRequestImpl("STARTTLS", null);
    }
}
