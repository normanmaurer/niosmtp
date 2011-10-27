package me.normanmaurer.niosmtp.transport.impl;

import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPResponseImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

public class Responder {
    public SMTPResponse repond(SMTPClientSession session, SMTPRequest request) {
        return new SMTPResponseImpl(250);
    }
    
}