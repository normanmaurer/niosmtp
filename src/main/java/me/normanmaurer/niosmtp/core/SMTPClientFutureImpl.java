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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Basic {@link SMTPClientFuture} implementation
 * 
 * @author Norman Maurer
 *
 */
public class SMTPClientFutureImpl<E> extends AbstractSMTPClientFuture<E>{
    
    private volatile boolean isReady = false;
    private volatile boolean isCancelled = false;
    private final AtomicReference<E> result = new AtomicReference<E>();
    private final boolean cancelable;
    private int waiters;
        
    
    public SMTPClientFutureImpl(boolean cancelable) {
        this.cancelable = cancelable;
    }
    
    public SMTPClientFutureImpl() {
        this(true);
    }
    
    /**
     * Set the <code>E</code> for the future and notify all waiting threads + the listeners. 
     *  
     * @param result
     */
    public void setResult(E result) {
        boolean fireListeners = false;
        if (!isDone()) {
            fireListeners = this.result.compareAndSet(null, result);
            isReady = true;
            synchronized (this) {
                if (waiters > 0) {
                    notifyAll();
                }
            }
                
        }
        if (fireListeners) {
            notifyListeners();
        }
        
    	
    }

    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!cancelable || isDone()) {
            return false;
        } else {
            isCancelled = true;
            SMTPClientSession session = getSession();
            if (session != null) {
                session.close();
            }
            notifyListeners();
           return true;
        }
    }
    
    private synchronized void checkReady() throws InterruptedException {
        
        while (!isReady) {
            try {
                waiters++;
                wait();
            } finally {
                waiters--;
            }
        }
    }

    private synchronized void checkReady(long timeout) throws InterruptedException {
        while (!isReady) {
            try {
                waiters++;
                wait(timeout);
            } finally {
                waiters--;
            }
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
    public E get() throws InterruptedException, ExecutionException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        checkReady();
        return getNoWait();
    }



    @Override
    public E get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        checkReady(unit.toMillis(timeout));
        return getNoWait();
    }

    @Override
    public E getNoWait() {
        return result.get();
    }



}
