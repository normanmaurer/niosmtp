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

import me.normanmaurer.niosmtp.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.SMTPResponse;

class DeliveryRecipientStatusImpl implements DeliveryRecipientStatus{

    private String address;
    private SMTPResponse response;

    public DeliveryRecipientStatusImpl(String address, SMTPResponse response) {
        
        this.address = address;
        this.response = response;
    }
    
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
    public Status getStatus() {
        int code = response.getCode();
        if (code >= 200 && code <= 299) {
            return Status.Ok;
        } else if (code >= 400 && code <= 499) {
            return Status.TemporaryError;
        } else {
            return Status.PermanentError;
        }
    }

}
