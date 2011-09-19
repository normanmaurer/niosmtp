/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* Selene licenses this file to You under the Apache License, Version 2.0
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import me.normanmaurer.niosmtp.DeliveryResult;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;

public class SMTPClientFutureImpl implements SMTPClientFuture{
    
    private boolean isReady = false;
    private boolean isCancelled = false;
    private final List<SMTPClientFutureListener> listeners = Collections.synchronizedList(new ArrayList<SMTPClientFutureListener>());
    private DeliveryResult result;
    
    /**
     * Set the {@link DeliveryResult} for the future and notify all waiting threads + the listeners
     * 
     * @param result
     */
    public synchronized void setDeliveryStatus(DeliveryResult result) {
        if (!isDone()) {
            this.result = result;
            isReady = true;
            notify();

            
            // notify the listeners
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).operationComplete(result);
            }
        } else {
            throw new IllegalStateException("Should not get called after future is ready");
        }
    }

    
    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (isCancelled() || isDone()) {
            return false;
        } else {
            doCancel(mayInterruptIfRunning);
           isCancelled = true;
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
    public synchronized boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public synchronized boolean isDone() {
        return isReady;
    }

    @Override
    public void addListener(SMTPClientFutureListener listener) {
        listeners.add(listener);
        if (isDone()) {
            listener.operationComplete(result);
        }
    }

    @Override
    public void removeListener(SMTPClientFutureListener listener) {
        listeners.remove(listener);
    }


    @Override
    public DeliveryResult get() throws InterruptedException, ExecutionException {
        checkReady();
        return result;
    }



    @Override
    public DeliveryResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        checkReady(unit.toMillis(timeout));
        if (isDone()) {
            return result;
        } else {
            return null;
        }
    }
    
    /**
     * Cancel the future. This should get overridden by sub-classes if need. By default this does nothing
     * 
     * @param mayInterruptIfRunning
     */
    protected void doCancel(boolean mayInterruptIfRunning) {
        
    }

}
