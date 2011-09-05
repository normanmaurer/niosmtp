package me.normanmaurer.niosmtp.impl.internal;

import java.net.InetAddress;

import me.normanmaurer.niosmtp.SMTPClientConfig;

/**
 * Simple {@link SMTPClientConfig} implementation which allows
 * to handle the config via a POJO.
 * 
 * @author Norman Maurer
 *
 */
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
    
    
    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getHeloName()
     */
    public String getHeloName() {
        return heloName;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getTimeout()
     */
    public int getTimeout() {
        return timeout;
    }
    
    public void setType(Type type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getType()
     */
    public Type getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#getLocalAddress()
     */
    public InetAddress getLocalAddress() {
        return localAddress;
    }
    
    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClientConfig#usePipelining()
     */
    public boolean usePipelining() {
        return usePipelining;
    }
    
    public void setUsePipelining(boolean usePipelining) {
        this.usePipelining = usePipelining;
    }

}
