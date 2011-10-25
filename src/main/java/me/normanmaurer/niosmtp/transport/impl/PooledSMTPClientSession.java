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
package me.normanmaurer.niosmtp.transport.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;


import org.slf4j.Logger;

/**
 * {@link SMTPClientSession} which should be used in the context of a pool implementation
 * 
 * @author Norman Maurer
 *
 */
public final class PooledSMTPClientSession implements SMTPClientSession {

    private final SMTPClientSession session;
    private final AtomicBoolean inUse = new AtomicBoolean(true);
    private final List<CloseListener> cListeners = new ArrayList<CloseListener>();
    private final AtomicLong lastSent = new AtomicLong(System.currentTimeMillis());

    public PooledSMTPClientSession(SMTPClientSession session) {
        this.session = session;
    }
    
    
    /**
     * Try to acquire this {@link PooledSMTPClientSession} for use. This will return <code>true</code> if the aquire was successful.
     * 
     * @return
     */
    public boolean acquire() {
        return inUse.compareAndSet(false, true);
    }
    
    /**
     * Return <code>true</code> if the {@link PooledSMTPClientSession} is currently in use
     * 
     * @return inUse
     */
    public boolean isInUse() {
        return inUse.get();
    }
    
    /**
     * Release the {@link PooledSMTPClientSession} and so make it possible for others to acquire it
     * 
     */
    public void release() {
        inUse.set(false);
    }
    
    @Override
    public SMTPDeliveryMode getDeliveryMode() {
        return session.getDeliveryMode();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return session.getAttributes();
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return session.getSupportedExtensions();
    }

    @Override
    public void setSupportedExtensions(Set<String> extensions) {
        session.setSupportedExtensions(extensions);
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public Logger getLogger() {
        return session.getLogger();
    }

    @Override
    public boolean isEncrypted() {
        return session.isEncrypted();
    }

    @Override
    public void startTLS() {
        // not allowed with pooled connection
    }
    
    private void setLastSent() {
        lastSent.set(System.currentTimeMillis());
    }
    
    @Override
    public void send(SMTPRequest request, final SMTPResponseCallback callback) {
        setLastSent();
        
        // listen for "QUIT" requests and if so use a RSET in place of it
        if (request.getCommand().equalsIgnoreCase("QUIT")) {
            session.send(SMTPRequestImpl.rset(), callback);
        } else {
            session.send(request, new PooledCallback(this, callback));
        }
    }

    @Override
    public void send(MessageInput request, final SMTPResponseCallback callback) {
        setLastSent();
        
        session.send(request, new PooledCallback(this, callback));
        
    }

    @Override
    public void close() {
        for(CloseListener listener: cListeners) {
            listener.onClose(this);
        }
        release();
    }

    @Override
    public boolean isClosed() {
        return session.isClosed();
    }

    @Override
    public SMTPClientConfig getConfig() {
        return session.getConfig();
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        cListeners.add(listener);
    }


    @Override
    public InetSocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
    }


    @Override
    public InetSocketAddress getLocalAddress() {
        return session.getLocalAddress();
    }


    @Override
    public void removeCloseListener(CloseListener listener) {
        session.removeCloseListener(listener);
    }


    @Override
    public Iterator<CloseListener> getCloseListeners() {
        return session.getCloseListeners();
    }
    
    /**
     * Return the last time (in ms) when a sent operation was triggered for the {@link PooledSMTPClientSession}
     * 
     * @return sent
     */
    public long getLastSent() {
        return lastSent.get();
    }
    
    /**
     * Return the wrapped {@link SMTPClientSession}
     * 
     * @return wrapped
     */
    public SMTPClientSession getWrapped() {
        return session;
    }
    
    
    
    
    
    /**
     * {@link SMTPResponseCallback} which forwards the actions to a given {@link SMTPResponseCallback}  but uses
     * the given {@link PooledSMTPClientSession} 
     * 
     * @author Norman Maurer
     *
     */
    private final static class PooledCallback implements SMTPResponseCallback {
        private final SMTPResponseCallback callback;
        private final PooledSMTPClientSession pooledSession;

        public PooledCallback(PooledSMTPClientSession pooledSession, SMTPResponseCallback callback) {
            this.pooledSession = pooledSession;
            this.callback = callback;
        }
        
        @Override
        public void onResponse(SMTPClientSession session, SMTPResponse response) {
            callback.onResponse(pooledSession, response);
        }
        
        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            callback.onException(pooledSession, t);
        }
    }
}