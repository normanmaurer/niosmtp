package me.normanmaurer.niosmtp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.normanmaurer.niosmtp.RecipientStatus;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPFutureListener;

public class SMTPClientFutureImpl implements SMTPClientFuture{

    private volatile boolean isReady = false;
    private volatile boolean isCancelled = false;
    private List<RecipientStatus> status = Collections.synchronizedList(new ArrayList<RecipientStatus>());
    public void done() {
        isReady = true;
        notify();
    }
    
    @Override
    public boolean cancel(boolean arg0) {
        return false;
    }

    private synchronized void checkReady() throws InterruptedException {
        while (!isReady) {
            wait();

        }
    }

    private synchronized void checkReady(long timeout) throws InterruptedException {
        while (!isReady) {
            wait(timeout);
        }
    }
    
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean isDone() {
        return isReady;
    }

    @Override
    public void addListener(SMTPFutureListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeListener(SMTPFutureListener listener) {
        // TODO Auto-generated method stub
        
    }

    public void addRecipientStatus(RecipientStatus rcptStatus) {
        status.add(rcptStatus);
    }


    @Override
    public Iterator<RecipientStatus> get() throws InterruptedException, ExecutionException {
        checkReady();
        return status.iterator();
    }



    @Override
    public Iterator<RecipientStatus> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        checkReady(unit.toMillis(timeout));
        if (isDone()) {
            return status.iterator();
        } else {
            return null;
        }
    }

}
