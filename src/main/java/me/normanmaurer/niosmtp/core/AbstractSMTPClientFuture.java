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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * 
 * Abstract base class for {@link SMTPClientFuture} implementations 
 * 
 * @author Norman Maurer
 *
 * @param <E>
 */
public abstract class AbstractSMTPClientFuture<E> implements SMTPClientFuture<E>{

    private List<SMTPClientFutureListener<E>> listeners;
    private volatile SMTPClientSession session;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSMTPClientFuture.class);
    
    /**
     * Notify all registered {@link SMTPClientFutureListener}'s
     */
    protected final void notifyListeners() {
        Iterator<SMTPClientFutureListener<E>> it = null;
        synchronized (this) {
            if (listeners != null) {
                // notify the listeners
                it  = listeners.iterator();

                listeners = null;
            }
        }
        if (it != null) {
            while(it.hasNext()) {
                SMTPClientFutureListener<E> listener = null;
                try {
                    listener = it.next();
                    listener.operationComplete(this);
                } catch (Throwable e) {
                    LOGGER.warn("Exception thrown while executing " + SMTPClientFutureListener.class.getName() + " " + listener, e);
                }
            }
        }
    }

    @Override
    public final synchronized void addListener(SMTPClientFutureListener<E> listener) {

        if (isDone()) {
            listener.operationComplete(this);
        } else {
            if (listeners == null) {
                listeners = new ArrayList<SMTPClientFutureListener<E>>();
            }
            listeners.add(listener);

        }
    }

    @Override
    public final synchronized void removeListener(SMTPClientFutureListener<E> listener) {
        if (!isDone()) {
            if (listeners != null) {
                listeners.remove(listener);
            }
            
        }
    }
    

    @Override
    public final SMTPClientSession getSession() {
        return session;
    }

    /**
     * Set the {@link SMTPClientSession} which does belong to this {@link SMTPClientFuture}
     * 
     * @param session
     */
    public final void setSMTPClientSession(SMTPClientSession session) {
        this.session = session;
    }

}
