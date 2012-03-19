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

import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.delivery.chain.SMTPClientFutureListenerFactory;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Constants for temporary data which will get stored in the {@link SMTPClientSession#getAttributes()} while sending the message
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPDeliverySessionConstants {

    /**
     * Key under which the {@link SMTPClientFutureImpl} is stored
     */
    public static final String FUTURE_KEY = "future";
   
    /**
     * Key under which the current recipient (RCPT TO) is stored as a {@link String}
     */
    public static final String CURRENT_RCPT_KEY = "cur_rcpt";
    
    
    /**
     * Key under which the recipients (RCPT TO) is stored as a {@link Iterator}
     */
    public static final String RECIPIENTS_KEY = "recipients";
    
    /**
     * Key under which the already processed {@link DeliveryRecipientStatus} are stored in a {@link List}
     */
    public static final String DELIVERY_STATUS_KEY = "deliverstatus";
    
    /**
     * Key under which we store if <code>PIPELINING</code> is active. 
     */
    public final static String PIPELINING_ACTIVE_KEY = "pipelining_active";
    
    
    /**
     * Key under which the already processed results are stored as a {@link List}
     */
    public static final String DELIVERY_RESULT_LIST_KEY = "delivery_result_List";

    
    
    /**
     * Key under which the {@link SMTPDeliveryEnvelope}'s are stored as a {@link Iterator}
     */
    public final static String SMTP_TRANSACTIONS_KEY = "smtp_transactions";
    
    /**
     * Key under which the state of a transaction within a SMTP session is stored. Transactions are
     * "active" between the "MAIL FROM" and when the server responds to the "DATA".
     */
    public final static String SMTP_TRANSACTION_ACTIVE_KEY = "smtp_transaction_active";

    /**
     * Key under which the current {@link SMTPDeliveryEnvelope} is stored
     */
    public final static String CURRENT_SMTP_TRANSACTION_KEY = "cur_smtp_transaction";


    /**
     * Key under which the {@link SMTPClientFutureListenerFactory} will be stored
     */
    public final static String SMTP_CLIENT_FUTURE_LISTENER_FACTORY ="smtp_client_future_listener_factory";
}
