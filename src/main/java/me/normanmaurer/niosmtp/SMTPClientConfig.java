package me.normanmaurer.niosmtp;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Configuration which is used to deliver email via SMTP
 *
 * @author Norman Maurer
 *
 */
public interface SMTPClientConfig {

    
    /**
     * Return the name which will get used for the HELO/EHLO
     * 
     * @return name
     */
    public String getHeloName();
    
    /**
     * Return the connection timeout (in seconds) for the client
     * 
     * @return connectionTimeout
     */
    public int getConnectionTimeout();

    
    /**
     * Return the {@link InetAddress} which should get used to bind to or null if no specific should get used
     * 
     * @return local
     */
    public InetSocketAddress getLocalAddress();
 
    /**
     * Return <code>true</code> if the client should use PIPELINING if possible
     * 
     * @return pipelining
     */
    public boolean usePipelining();
}
