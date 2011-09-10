package me.normanmaurer.niosmtp.impl.internal;

import java.util.Iterator;

import me.normanmaurer.niosmtp.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.DeliveryResult;
import me.normanmaurer.niosmtp.SMTPException;

public class DeliveryResultImpl implements DeliveryResult{

    private final Iterable<DeliveryRecipientStatus> status;
    private final SMTPException exception;

    public DeliveryResultImpl(Iterable<DeliveryRecipientStatus> status) {
        this.status = status;
        this.exception = null;
    }
    
    public DeliveryResultImpl(SMTPException exception) {
        this.exception = exception;
        this.status = null;
    }
    
    @Override
    public boolean isSuccess() {
        return exception == null;
    }

    @Override
    public SMTPException getException() {
        return exception;
    }

    @Override
    public Iterator<DeliveryRecipientStatus> getRecipientStatus() {
        if (status == null) {
            return null;
        }
        return status.iterator();
    }

}
