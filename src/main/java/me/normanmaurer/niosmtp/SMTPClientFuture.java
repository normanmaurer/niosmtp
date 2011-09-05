package me.normanmaurer.niosmtp;

import java.util.Iterator;
import java.util.concurrent.Future;

/**
 * A {@link Future} which allows to register {@link SMTPClientFutureListener} and also make it possible to
 * access an {@link Iterator} which holds {@link DeliveryRecipientStatus} objects in a blocking mode.
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientFuture extends Future<Iterator<DeliveryRecipientStatus>>{

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
