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
package me.normanmaurer.niosmtp;

/**
 * A SMTP Request which is send to the SMTP-Server
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPRequest {
    
    public final static String STARTTLS_COMMAND = "STARTTLS";
    public final static String HELO_COMMAND = "HELO";
    public final static String EHLO_COMMAND = "EHLO";
    public final static String MAIL_COMMAND = "MAIL FROM";
    public final static String RCPT_COMMAND = "RCPT TO";
    public final static String AUTH_COMMAND = "AUTH";
    public final static String AUTH_PLAIN_ARGUMENT = "PLAIN";
    public final static String AUTH_LOGIN_ARGUMENT = "LOGIN";
    public final static String DATA_COMMAND = "DATA";
    public final static String QUIT_COMMAND = "QUIT";

    /**
     * Return the command 
     * 
     * @return command
     */
    String getCommand();
    
    /**
     * Return the argument. This may be null
     * 
     * @return argument
     */
    String getArgument();

    /**
     * Return the separator.
     *
     * @return separator
     */
    char getSeparator();
}
