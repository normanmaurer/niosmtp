package me.normanmaurer.niosmtp;

public interface SMTPResponse {
    public int getCode();

    public String getLastLine();
}
