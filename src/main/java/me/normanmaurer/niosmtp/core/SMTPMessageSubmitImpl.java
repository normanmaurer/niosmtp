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

import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPMessageSubmit;

/**
 * 
 * @author Norman Maurer
 */
public class SMTPMessageSubmitImpl implements SMTPMessageSubmit{

    private final SMTPMessage message;
    private final int recipients;

    public SMTPMessageSubmitImpl(SMTPMessage message, int recipients ) {
        this.message = message;
        this.recipients = recipients;
    }
    
    @Override
    public int getRecipients() {
        return recipients;
    }

    @Override
    public SMTPMessage getMessage() {
        return message;
    } 
    


}
