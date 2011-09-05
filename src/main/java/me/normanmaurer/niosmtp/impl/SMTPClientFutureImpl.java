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
import me.normanmaurer.niosmtp.SMTPClientFutureListener;

public class SMTPClientFutureImpl implements SMTPClientFuture{

    private volatile boolean isReady = false;
    private volatile boolean isCancelled = false;
    private final List<RecipientStatusImpl> status = Collections.synchronizedList(new ArrayList<RecipientStatusImpl>());
    private final List<SMTPClientFutureListener> listeners = Collections.synchronizedList(new ArrayList<SMTPClientFutureListener>());
    public synchronized void done() {
        if (!isReady) {
            isReady = true;
            notify();

            
            for (int i = 0; i < listeners.size(); i++) {
                final Iterator<RecipientStatusImpl> it = status.iterator();
                listeners.get(i).operationComplete(new Iterator<RecipientStatus>() {

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public RecipientStatus next() {
                        return it.next();
                    }

                    @Override
                    public void remove() {
                        it.remove();
                        
                    }
                });
            }
        } else {
            notify();
        }
    }
    
    protected List<RecipientStatusImpl> getStatus() {
        return status;
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
    public void addListener(SMTPClientFutureListener listener) {
        listeners.add(listener);
        if (isReady) {
            listener.operationComplete(new RecipientStatusIterator(status.iterator()));
        }
    }

    @Override
    public void removeListener(SMTPClientFutureListener listener) {
        listeners.remove(listener);
    }

    protected void addRecipientStatus(RecipientStatusImpl rcptStatus) {
        status.add(rcptStatus);
    }


    @Override
    public Iterator<RecipientStatus> get() throws InterruptedException, ExecutionException {
        checkReady();
        return new RecipientStatusIterator(status.iterator());
    }



    @Override
    public Iterator<RecipientStatus> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        checkReady(unit.toMillis(timeout));
        if (isDone()) {
            return new RecipientStatusIterator(status.iterator());
        } else {
            return null;
        }
    }
    
    private final class RecipientStatusIterator implements Iterator<RecipientStatus> {

        private final Iterator<RecipientStatusImpl> it;
        public RecipientStatusIterator(Iterator<RecipientStatusImpl> it) {
            this.it = it;
        }
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public RecipientStatus next() {
            return it.next();
        }

        @Override
        public void remove() {
            it.remove();
            
        }
        
    }

}
