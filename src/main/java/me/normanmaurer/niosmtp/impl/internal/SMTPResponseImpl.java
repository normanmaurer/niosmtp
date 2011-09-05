package me.normanmaurer.niosmtp.impl.internal;

import me.normanmaurer.niosmtp.SMTPResponse;

class SMTPResponseImpl implements SMTPResponse{

    private int code;
    private String line;

    public SMTPResponseImpl(int code, String line) {
        this.code = code;
        this.line = line;
    }
    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getLastLine() {
        return line;
    }
    
    public String toString() {
        return code + " " + line;
    }

}
