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

    private final List<SMTPClientFutureListener<E>> listeners = new CopyOnWriteArrayList<SMTPClientFutureListener<E>>();
    private volatile SMTPClientSession session;

    protected void notifyListeners() {
        // notify the listeners
        Iterator<SMTPClientFutureListener<E>> it = listeners.iterator();
        while(it.hasNext()) {
            it.next().operationComplete(this);
        }
    }

    @Override
    public void addListener(SMTPClientFutureListener<E> listener) {
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
    public SMTPClientSession getSession() {
        return session;
    }

    public void setSMTPClientSession(SMTPClientSession session) {
        this.session = session;
    }



    @Override
    public Iterator<SMTPClientFutureListener<E>> getListeners() {
        return listeners.iterator();
    }
}
