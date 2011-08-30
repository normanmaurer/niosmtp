package me.normanmaurer.niosmtp;

public interface SMTPRequest {
    
    public String getCommand();
    
    public String getArgument();
}
