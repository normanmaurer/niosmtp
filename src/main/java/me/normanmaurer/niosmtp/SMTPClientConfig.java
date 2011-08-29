package me.normanmaurer.niosmtp;

import java.net.InetAddress;

/**
 * Configuration which is used to deliver email via SMTP
 *
 * @author Norman Maurer
 *
 */
public interface SMTPClientConfig {

    /**
     * The type of the client
     *
     */
    public static enum Type {
        /**
         * Plain SMTP 
         */
        Plain,
        
        /**
         * Switch to TLS via STARTTLS if possible
         * 
         */
        Starttls,
        
        /**
         * Use SMTPS
         */
        Tls
    }
    
    /**
     * Return the name which will get used for the HELO/EHLO
     * 
     * @return name
     */
    public String getHeloName();
    
    /**
     * Return the timeout (in seconds) for the client
     * 
     * @return timeout
     */
    public int getTimeout();

    /**
     * Return the {@link Type} of the client
     * 
     * @return type
     */
    public Type getType();
    
    /**
     * Return the {@link InetAddress} which should get used to bind to or null if no specific should get used
     * 
     * @return local
     */
    public InetAddress getLocalAddress();
 
    /**
     * Return <code>true</code> if the client should use PIPELINING if possible
     * 
     * @return pipelining
     */
    public boolean usePipelining();
}
