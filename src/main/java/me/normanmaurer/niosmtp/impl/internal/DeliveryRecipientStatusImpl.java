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
    public boolean isSuccessful() {
        return response.getCode() < 300 &&  response.getCode() > 200;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public SMTPResponse getResponse() {
        return response;
    }

}
