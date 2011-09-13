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
package me.normanmaurer.niosmtp.impl.internal;

import java.nio.charset.Charset;



public interface SMTPClientConstants {
    
    /**
     * The {@link Charset} used for the SMTP protocol. This is <code>US-ASCII</code> (per rfc)
     */
    public final static Charset CHARSET = Charset.forName("US-ASCII");
    
    /**
     * CLRF sequence
     */
    public final static String CRLF = "\r\n";
    
    /**
     * Identifier used to detect if the SMTP Server supports <code>PIPELINING</code>
     */
    public final static String PIPELINING_EXTENSION = "PIPELINING";
    
    
    public static final String FUTURE_KEY = "FUTURE";
    public static final String MAIL_FROM_KEY = "MAIL_FROM";
    public static final String RECIPIENTS_KEY = "RECIPIENTS";
    public static final String SMTP_CONFIG_KEY = "SMTP_CONFIG";
    public static final String MSG_KEY = "MSG";
    public static final String RECIPIENT_STATUS_LIST_KEY = "RECIPIENT_STATUS_LIST";
    public static final String SUPPORTS_PIPELINING_KEY = "SUPPORTS_PIPELINING";
    public static final String LAST_RECIPIENT_KEY = "LAST_RECIPIENT";
    public static final String SMTP_STATE_KEY = "SMTP_STATE";

}
