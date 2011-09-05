package me.normanmaurer.niosmtp;

import java.util.Iterator;

/**
 * A listener which will get informed once the SMTP delivery was done
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientFutureListener {

    /**
     * Callback which will get called once the operation was complete
     * 
     * @param status
     */
    void operationComplete(Iterator<DeliveryRecipientStatus> status); 
}
