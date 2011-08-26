package me.normanmaurer.niosmtp;

public interface RecipientStatus {

    public int getReturnCode();
    
    public String getResponse();
    
    public boolean isSuccessfull();
    
    public String getAddress();
}
