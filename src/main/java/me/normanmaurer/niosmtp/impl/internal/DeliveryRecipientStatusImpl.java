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
