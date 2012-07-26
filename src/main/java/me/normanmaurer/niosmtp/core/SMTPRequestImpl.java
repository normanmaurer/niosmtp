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
package me.normanmaurer.niosmtp.core;

import me.normanmaurer.niosmtp.SMTPRequest;

public class SMTPRequestImpl implements SMTPRequest{

    private final static SMTPRequest QUIT_REQUEST = new SMTPRequestImpl("QUIT", null);
    private final static SMTPRequest STARTTLS_REQUEST = new SMTPRequestImpl("STARTTLS", null);
    private final static SMTPRequest DATA_REQUEST = new SMTPRequestImpl("DATA", null);
    private final static SMTPRequest AUTH_LOGIN_REQUEST = new SMTPRequestImpl("AUTH", "LOGIN");
    private final static SMTPRequest AUTH_PLAIN_REQUEST = new SMTPRequestImpl("AUTH", "PLAIN");
    private final static SMTPRequest NOOP_REQUEST = new SMTPRequestImpl("NOOP", null);
    private final static SMTPRequest RSET_REQUEST = new SMTPRequestImpl("RSET", null);

    private final String command;
    private final String argument;
    private final char separator;

    
    public SMTPRequestImpl(String command, String argument, char separator) {
        this.command = command;
        this.argument = argument;
        this.separator = separator;
    }

    public SMTPRequestImpl(String command, String argument) {
        this(command, argument, ' ');
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
    public char getSeparator() {
        return separator;
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
        return QUIT_REQUEST;
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
        return new SMTPRequestImpl(RCPT_COMMAND, "<" + recipient + ">", ':');
    }
    
    
    /**
     * Create a <code>MAIL</code> {@link SMTPRequest} 
     * 
     * @param sender
     * @return mail
     */
    public static SMTPRequest mail(String sender) {
        if (sender == null) {
            sender = "";
        }
        return new SMTPRequestImpl(MAIL_COMMAND,  "<" + sender + ">", ':');
    }
    
    /**
     * Create a <code>DATA</code> {@link SMTPRequest}
     * 
     * @return data
     */
    public static SMTPRequest data() {
        return DATA_REQUEST;
    }
    
    /**
     * Create a <code>STARTTLS</code> {@link SMTPRequest}
     * 
     * @return starttls
     */
    public static SMTPRequest startTls() {
        return STARTTLS_REQUEST;
    }
    
    /**
     * Create a <code>AUTH PLAIN</code> {@link SMTPRequest}
     * 
     * @return authPlain
     */
    public static SMTPRequest authPlain() {
        return AUTH_PLAIN_REQUEST;
    }
    
    
    /**
     * Create a <code>AUTH LOGIN</code> {@link SMTPRequest}
     * 
     * @return authLogin
     */
    public static SMTPRequest authLogin() {
        return AUTH_LOGIN_REQUEST;
    }
    
    /**
     * Create a <code>NOOP</code> {@link SMTPRequest}
     * 
     * @return noop
     */
    public static SMTPRequest noop() {
        return NOOP_REQUEST;
    }
    /**
     * Create a <code>RSET</code> {@link SMTPRequest}
     * 
     * @return rset
     */
    public static SMTPRequest rset() {
        return RSET_REQUEST;
    }
}
