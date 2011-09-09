package me.normanmaurer.niosmtp;

import java.util.concurrent.Future;

/**
 * A {@link Future} which allows to register {@link SMTPClientFutureListener} and also make it possible to
 * access the {@link DeliveryResult} objects in a blocking fashion.
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientFuture extends Future<DeliveryResult>{

    /**
     * Add the {@link SMTPClientFutureListener} which will notified one the delivery is complete.
     * 
     * If the {@link SMTPClientFuture} was already completed it will notify the {@link SMTPClientFutureListener} directly
     * 
     * 
     * @param listener
     */
    public void addListener(SMTPClientFutureListener listener);
    
    /**
     * Remove the {@link SMTPClientFutureListener}
     * 
     * @param listener
     */
    public void removeListener(SMTPClientFutureListener listener);
}
