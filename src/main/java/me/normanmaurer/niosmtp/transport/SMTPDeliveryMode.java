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



/**
 * Different delivery modes which are valid for SMTP
 * 
 * @author Norman Maurer
 *
 */
public enum SMTPDeliveryMode {
    /**
     * Plain delivery (not encrypted)
     */
    PLAIN,
    
    /**
     * SMTPS Delivery (encrypted via TLS/SSL)
     */
    SMTPS,
    
    /**
     * Try to use STARTTLS (encrypt after connection was established)
     */
    STARTTLS_TRY,
    
    /**
     * Use STARTTLS (encrypt after connect was established) and fail if STARTTLS is not supported by the 
     * remote SMTP server
     */
    STARTTLS_DEPEND
}
