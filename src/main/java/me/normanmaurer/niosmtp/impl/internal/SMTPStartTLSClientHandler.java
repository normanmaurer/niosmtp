package me.normanmaurer.niosmtp.impl.internal;

import java.io.InputStream;
import java.util.LinkedList;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPClientConfig;

public class SMTPStartTLSClientHandler extends SMTPClientHandler{

    private SSLEngine engine;
    private boolean dependOnStartTLS;

    public SMTPStartTLSClientHandler(SMTPClientFutureImpl future, String mailFrom, LinkedList<String> recipients, InputStream msg, SMTPClientConfig config, boolean dependOnStartTLS, SSLEngine engine) {
        super(future, mailFrom, recipients, msg, config);
        this.dependOnStartTLS = dependOnStartTLS;
        this.engine = engine;
    }

}
