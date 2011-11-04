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
package me.normanmaurer.niosmtp.delivery.impl;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;

/**
 * Simple POJO implementation of {@link DeliveryRecipientStatus}
 * 
 * 
 * @author Norman Maurer
 *
 */
public class DeliveryRecipientStatusImpl implements DeliveryRecipientStatus{

    private final String address;
    private SMTPResponse response;

    public DeliveryRecipientStatusImpl(String address, SMTPResponse response) {
        
        this.address = address;
        this.response = response;
    }
    
    /**
     * Set the {@link SMTPResponse} for the address
     * 
     * @param response
     */
    public void setResponse(SMTPResponse response) {
        this.response = response;
    }
    


    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public SMTPResponse getResponse() {
        return response;
    }

    @Override
    public DeliveryStatus getStatus() {
        int code = response.getCode();
        if (code >= 200 && code <= 299) {
            return DeliveryStatus.Ok;
        } else if (code >= 400 && code <= 499) {
            return DeliveryStatus.TemporaryError;
        } else {
            return DeliveryStatus.PermanentError;
        }
    }

}
