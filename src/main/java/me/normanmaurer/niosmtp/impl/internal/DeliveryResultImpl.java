package me.normanmaurer.niosmtp.impl.internal;

import java.util.Collection;
import java.util.Iterator;

import me.normanmaurer.niosmtp.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.DeliveryResult;

public class DeliveryResultImpl implements DeliveryResult{

    private final Collection<DeliveryRecipientStatus> status;
    private final Throwable cause;

    public DeliveryResultImpl(Collection<DeliveryRecipientStatus> status) {
        this.status = status;
        this.cause = null;
    }
    
    public DeliveryResultImpl(Throwable cause) {
        this.cause = cause;
        this.status = null;
    }
    
    @Override
    public boolean isSuccess() {
        return cause == null;
    }

    @Override
    public Throwable getCause() {
        return null;
    }

    @Override
    public Iterator<DeliveryRecipientStatus> getRecipientStatus() {
        if (status == null) {
            return null;
        }
        return status.iterator();
    }

}
