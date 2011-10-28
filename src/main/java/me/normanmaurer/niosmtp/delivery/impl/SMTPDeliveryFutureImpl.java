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
package me.normanmaurer.niosmtp.delivery.impl;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.normanmaurer.niosmtp.delivery.DeliveryResult;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryFuture;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryFutureListener;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Basic {@link SMTPDeliveryFuture} implementation 
 * @author Norman Maurer
 *
 */
public class SMTPDeliveryFutureImpl implements SMTPDeliveryFuture{
    
    private volatile boolean isReady = false;
    private volatile boolean isCancelled = false;
    private final List<SMTPDeliveryFutureListener> listeners = new CopyOnWriteArrayList<SMTPDeliveryFutureListener>();
    private volatile Iterable<DeliveryResult> result;
    private volatile SMTPClientSession session;
        
    /**
     * Set the {@link DeliveryResult} for the future and notify all waiting threads + the listeners. This should get called only on time, 
     * otherwise it will throw an {@link IllegalStateException}
     * 
     * @param result
     */
    public void setDeliveryStatus(Iterable<DeliveryResult> result) {
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
            Iterator<SMTPDeliveryFutureListener> it = listeners.iterator();
            while(it.hasNext()) {
                it.next().operationComplete(this);
            }
        }
        
    	
    }

    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        } else {
            isCancelled = true;
            if (session != null) {
                session.close();
            }
            Iterator<SMTPDeliveryFutureListener> it = listeners.iterator();
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
    public  void addListener(SMTPDeliveryFutureListener listener) {
        listeners.add(listener);
        if (isDone()) {
            listener.operationComplete(this);
        }
    }

    @Override
    public void removeListener(SMTPDeliveryFutureListener listener) {
        listeners.remove(listener);
    }


    @Override
    public Iterator<DeliveryResult> get() throws InterruptedException, ExecutionException {
        checkReady();
        return result.iterator();
    }



    @Override
    public Iterator<DeliveryResult> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        checkReady(unit.toMillis(timeout));
        return getNoWait();
    }

    public void setSMTPClientSession(SMTPClientSession session) {
        this.session = session;
    }


    @Override
    public Iterator<DeliveryResult> getNoWait() {
        if (result == null) {
            return null;
        } else {
            return result.iterator();
        }
    }


    @Override
    public SMTPClientSession getSession() {
        return session;
    }


    @Override
    public Iterator<SMTPDeliveryFutureListener> getListeners() {
        return listeners.iterator();
    }

}
