package me.normanmaurer.niosmtp;

import java.util.Iterator;

/**
 * Result of an email delivery
 * 
 * @author Norman Maurer
 *
 */
public interface DeliveryResult {

    /**
     * Return true if the delivery was successful (no exception)
     * 
     * @return success
     */
    public boolean isSuccess();
    
    /**
     * Return the {@link Throwable} which was thrown while try to deliver
     * 
     * @return cause
     */
    public Throwable getCause();
    
    /**
     * Return an {@link Iterator} which holds all {@link DeliveryRecipientStatus} objects for the 
     * delivery. This MAY return null if {@link #getCause()} returns not null
     * 
     * @return status
     */
    public Iterator<DeliveryRecipientStatus> getRecipientStatus();
}
