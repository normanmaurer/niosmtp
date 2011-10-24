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
package me.normanmaurer.niosmtp.transport;

import java.nio.charset.Charset;


/**
 * Constants which are used within the SMTP context
 * 
 * @author Norman Mauurer
 *
 */
public interface SMTPClientConstants {
    
    /**
     * The {@link Charset} used for the SMTP protocol. This is <code>US-ASCII</code> (per rfc)
     */
    public final static Charset CHARSET = Charset.forName("US-ASCII");
    
    
    /**
     * Identifier used to detect if the SMTP Server supports <code>PIPELINING</code>
     */
    public final static String PIPELINING_EXTENSION = "PIPELINING";
    
    /**
     * Identifier used to detect if the SMTP Server supports <code>STARTTLS</code>
     */
    public final static String STARTTLS_EXTENSION = "STARTTLS";
    
    /**
     * Identifier used to detect if the SMTP Server supports <code>8BITMIME</code>
     */
    public final static String _8BITMIME_EXTENSION = "8BITMIME";
    
    

}
