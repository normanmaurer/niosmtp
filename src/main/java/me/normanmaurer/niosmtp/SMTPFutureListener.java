package me.normanmaurer.niosmtp;

public interface SMTPFutureListener {

    void operationComplete(SMTPFuture future) throws Exception; 
}
