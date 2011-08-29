package me.normanmaurer.niosmtp.impl;

import java.net.InetAddress;

import me.normanmaurer.niosmtp.SMTPClientConfig;

public class SMTPClientConfigImpl implements SMTPClientConfig {

    private String heloName = "localhost";
    private int timeout = 60;
    private Type type = Type.Plain;
    private InetAddress localAddress = null;
    private boolean usePipelining = true;
    
    public SMTPClientConfigImpl() {
    }
    
    public void setHeloName(String heloName) {
        this.heloName = heloName;
    }
    
    
    public String getHeloName() {
        return heloName;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public InetAddress getLocalAddress() {
        return localAddress;
    }
    
    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    public boolean usePipelining() {
        return usePipelining;
    }
    
    public void setUsePipelining(boolean usePipelining) {
        this.usePipelining = usePipelining;
    }

}
