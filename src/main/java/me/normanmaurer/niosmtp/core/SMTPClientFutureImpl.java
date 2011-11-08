/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* niosmtp licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package me.normanmaurer.niosmtp.core;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Basic {@link SMTPClientFuture} implementation 
 * @author Norman Maurer
 *
 */
public class SMTPClientFutureImpl<E> implements SMTPClientFuture<E>{
    
    private volatile boolean isReady = false;
    private volatile boolean isCancelled = false;
    private final List<SMTPClientFutureListener<E>> listeners = new CopyOnWriteArrayList<SMTPClientFutureListener<E>>();
    private volatile E result;
    private volatile SMTPClientSession session;
    private final boolean cancelable;
        
    
    public SMTPClientFutureImpl(boolean cancelable) {
        this.cancelable = cancelable;
    }
    
    public SMTPClientFutureImpl() {
        this(true);
    }
    
    /**
     * Set the <code>E</code> for the future and notify all waiting threads + the listeners. This should get called only on time, 
     * otherwise it will throw an {@link IllegalStateException}
     * 
     * @param result
     */
    public void setDeliveryStatus(E result) {
        boolean fireListeners = false;
        if (!isDone()) {
            this.result = result;
            isReady = true;
            fireListeners = true;
            synchronized (this) {
                notify();
            }
                
        }
        if (fireListeners) {
            // notify the listeners
            Iterator<SMTPClientFutureListener<E>> it = listeners.iterator();
            while(it.hasNext()) {
                it.next().operationComplete(this);
            }
        }
        
    	
    }

    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!cancelable || isDone()) {
            return false;
        } else {
            isCancelled = true;
            if (session != null) {
                session.close();
            }
            Iterator<SMTPClientFutureListener<E>> it = listeners.iterator();
            while(it.hasNext()) {
                it.next().operationComplete(this);
            }
           return true;
        }
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
    public  boolean isDone() {
        return isReady || isCancelled;
    }

    @Override
    public  void addListener(SMTPClientFutureListener<E> listener) {
        listeners.add(listener);
        if (isDone()) {
            listener.operationComplete(this);
        }
    }

    @Override
    public void removeListener(SMTPClientFutureListener<E> listener) {
        listeners.remove(listener);
    }


    @Override
    public E get() throws InterruptedException, ExecutionException {
        checkReady();
        return getNoWait();
    }



    @Override
    public E get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        checkReady(unit.toMillis(timeout));
        return getNoWait();
    }

    public void setSMTPClientSession(SMTPClientSession session) {
        this.session = session;
    }


    @Override
    public E getNoWait() {
        return result;
    }


    @Override
    public SMTPClientSession getSession() {
        return session;
    }


    @Override
    public Iterator<SMTPClientFutureListener<E>> getListeners() {
        return listeners.iterator();
    }

}
