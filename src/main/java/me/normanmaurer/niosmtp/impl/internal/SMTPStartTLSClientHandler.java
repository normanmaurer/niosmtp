package me.normanmaurer.niosmtp.impl.internal;

import java.io.InputStream;
import java.util.LinkedList;

import me.normanmaurer.niosmtp.SMTPClientConfig;

public class SMTPStartTLSClientHandler extends SMTPClientHandler{

    public SMTPStartTLSClientHandler(SMTPClientFutureImpl future, String mailFrom, LinkedList<String> recipients, InputStream msg, SMTPClientConfig config) {
        super(future, mailFrom, recipients, msg, config);
    }

}
