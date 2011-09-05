package me.normanmaurer.niosmtp;

import java.util.Iterator;

public interface SMTPFutureListener {

    void operationComplete(Iterator<RecipientStatus> status); 
}
