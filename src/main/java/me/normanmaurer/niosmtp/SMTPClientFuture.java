package me.normanmaurer.niosmtp;

import java.util.Iterator;
import java.util.concurrent.Future;

public interface SMTPClientFuture extends Future<Iterator<RecipientStatus>>{

    public void addListener(SMTPFutureListener listener);
    
    public void removeListener(SMTPFutureListener listener);
}
