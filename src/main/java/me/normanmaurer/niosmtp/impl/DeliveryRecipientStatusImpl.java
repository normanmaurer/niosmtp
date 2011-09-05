package me.normanmaurer.niosmtp.impl;

import me.normanmaurer.niosmtp.DeliveryRecipientStatus;

public class DeliveryRecipientStatusImpl implements DeliveryRecipientStatus{

    private String address;
    private String response;
    private int code;

    public DeliveryRecipientStatusImpl(String address, int code, String response) {
        
        this.address = address;
        this.code = code;
        this.response = response;
    }
    
    public void setResponse(int code, String response) {
        this.code = code;
        this.response = response;
    }
    
    @Override
    public int getReturnCode() {
        return code;
    }

    @Override
    public String getResponse() {
        return response;
    }

    @Override
    public boolean isSuccessful() {
        return code < 300 && code > 200;
    }

    @Override
    public String getAddress() {
        return address;
    }

}
