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
package me.normanmaurer.niosmtp.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Basic {@link SMTPClientFuture} implementation 
 * @author Norman Maurer
 *
 */
public class SMTPClientFutureImpl implements SMTPClientFuture{
    
    private boolean isReady = false;
    private boolean isCancelled = false;
    private final List<SMTPClientFutureListener> listeners = new ArrayList<SMTPClientFutureListener>();
    private Iterable<DeliveryResult> result;
    private SMTPClientSession session;
    
    /**
     * Set the {@link DeliveryResult} for the future and notify all waiting threads + the listeners. This should get called only on time, 
     * otherwise it will throw an {@link IllegalStateException}
     * 
     * @param result
     * @throws illegalStateException
     */
    public synchronized void setDeliveryStatus(Iterable<DeliveryResult> result) {
        if (!isDone()) {
            this.result = result;
            isReady = true;
            notify();

            
            // notify the listeners
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).operationComplete(result.iterator());
            }
        }
    }

    
    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (isCancelled() || isDone()) {
            return false;
        } else {
           session.close();
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
        return isReady || isCancelled;
    }

    @Override
    public synchronized void addListener(SMTPClientFutureListener listener) {
        listeners.add(listener);
        if (isDone()) {
            listener.operationComplete(result.iterator());
        }
    }

    @Override
    public synchronized void removeListener(SMTPClientFutureListener listener) {
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
        if (isDone()) {
            return result.iterator();
        } else {
            return null;
        }
    }

    public synchronized void setSMTPClientSession(SMTPClientSession session) {
        this.session = session;
    }

}
