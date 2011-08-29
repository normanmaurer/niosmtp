package me.normanmaurer.niosmtp;

import java.util.concurrent.Future;

public interface SMTPClientFuture extends Future<RecipientStatus>{

    public void addListener(SMTPFutureListener listener);
    
    public void removeListener(SMTPFutureListener listener);
}
