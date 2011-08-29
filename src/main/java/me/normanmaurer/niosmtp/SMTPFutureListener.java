package me.normanmaurer.niosmtp;

public interface SMTPFutureListener {

    void operationComplete(SMTPClientFuture future) throws Exception; 
}
