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
package me.normanmaurer.niosmtp;

import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Callback which will get executed in an async manner
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPResponseCallback {
    
    
    /**
     * {@link SMTPResponseCallback} which just do nothing
     */
    public final static SMTPResponseCallback EMPTY = new SMTPResponseCallback() {
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            // Do nothing
        }
        
        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            // Do nothing
        }
    };
    
    /**
     * Method which will get executed once the {@link SMTPResponse} was received
     * 
     * @param session
     * @param response
     */
    public void onResponse(SMTPClientSession session, SMTPResponse response);
    
    /**
     * Method which will get executed if an {@link Exception} was thrown. Be Aware that the {@link SMTPClientSession} can be null if 
     * the Connection was not 100% established
     * 
     * @param session
     * @param t
     */
    public void onException(SMTPClientSession session, Throwable t);
    
}

