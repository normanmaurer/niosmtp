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

import me.normanmaurer.niosmtp.SMTPResponse;

/**
 * Status for the delivery of an email to a recipient
 * 
 * @author Norman Maurer
 *
 */
public interface DeliveryRecipientStatus {

    public enum DeliveryStatus{
        /**
         * Email was successful delivered to the recipient
         * 
         */
        Ok,
        
        /**
         * Email was permanent reject for the recipient
         */
        PermanentError,
        
        /**
         * Email was temporar reject for the recipient
         */
        TemporaryError
    }
    
    /**
     * Return the {@link SMTPResponse} which was returned for the recipient. 
     * 
     * @return response
     */
    SMTPResponse getResponse();
    

    /**
     * Return the status
     * 
     * @return status
     */
    DeliveryStatus getStatus();
    
    /**
     * Return the email-address of the recipient
     * 
     * @return address
     */
    String getAddress();
}
