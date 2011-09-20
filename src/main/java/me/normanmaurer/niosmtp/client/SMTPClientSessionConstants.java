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

import java.util.LinkedList;
import java.util.List;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Constants for temporary data which will get stored in the {@link SMTPClientSession#getAttributes()} while sending the message
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientSessionConstants {

    /**
     * Key under which the {@link SMTPClientFutureImpl} is stored
     */
    public static final String FUTURE_KEY = "future";
   
    /**
     * Key under which the sender (MAIL FROM) is stored as a {@link String}
     */
    public static final String SENDER_KEY = "sender";
    
    /**
     * Key under which the current recipient (RCPT TO) is stored as a {@link String}
     */
    public static final String CURRENT_RCPT_KEY = "cur_rcpt";
    
    /**
     * Key under which the working copy of the to-processing recipients are stored as a {@link LinkedList} which holds the recipients
     * as {@link String} objects
     */
    public static final String RECIPIENTS_KEY = "rcpts";
    
    /**
     * Key under which the {@link MessageInput} is stored 
     */
    public static final String MSG_KEY = "msg";
    
    /**
     * Key under which the already processed {@link DeliveryRecipientStatus} are stored in a {@link List}
     */
    public static final String DELIVERY_STATUS_KEY = "deliverstatus";
    
    /**
     * Key under which we store if <code>PIPELINING</code> is active. 
     */
    public final static String PIPELINING_ACTIVE_KEY = "PIPELINING_ACTIVE";

}
