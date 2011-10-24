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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientSession.CloseListener;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;


/**
 * An {@link SMTPClientTransport} which tries to "pool" the {@link SMTPClientSession} for re-use. Use it with caution if
 * you expect to connect to many different remote SMTP servers. This is best be used for the usage within the scope of a 
 * mail relay.
 * 
 * @author Norman Maurer
 *
 */
public class PooledSMTPClientTransport implements SMTPClientTransport{

    private final static String WELCOME_RESPONSE_KEY = "WELCOME_RESPONSE";    
    private final int keepAliveTimeInSec;
    private final SMTPClientTransport transport;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<PooledSMTPClientSession>> pooledSessions = new ConcurrentHashMap<String, ConcurrentLinkedQueue<PooledSMTPClientSession>>();
    private final ScheduledExecutorService noopSender;
    private final int keepAliveIntervalInSec;

    
    private final SMTPResponseCallback noopSchedulingResponseCallback = new SMTPResponseCallback() {

        @Override
        public void onResponse(final SMTPClientSession session, SMTPResponse response) {
            PooledSMTPClientSession pooledSession = (PooledSMTPClientSession) session;
            
            // Check if the pooled session was not closed and is not in use atm
            if (!pooledSession.isClosed() && !pooledSession.isInUse()) {
                
                // Check if the session is session can be used or if it was to long in the pool and so must get closed and removed
                if (pooledSession.getLastSent() < System.currentTimeMillis() - keepAliveTimeInSec) {
                    noopSender.schedule(new Runnable() {
                
                        @Override
                        public void run() {
                            session.send(SMTPRequestImpl.noop(), noopSchedulingResponseCallback);
                        }
                    }, keepAliveIntervalInSec, TimeUnit.SECONDS);
                } else {
                    
                    // make sure no other thread is grapping the session and close it
                    if (pooledSession.setInUse()) {
                        pooledSession.send(SMTPRequestImpl.quit(), SMTPResponseCallback.EMPTY);
                        pooledSession.close();
                        pooledSession.getWrapped().close();
                    }
                }
            }
        }

        @Override
        public void onException(SMTPClientSession session, Throwable t) {
            // close the session when receiving an Exception. 
            PooledSMTPClientSession pooledSession = (PooledSMTPClientSession) session;
            pooledSession.close();
            pooledSession.getWrapped().close();
        }
        
    };

    
    public PooledSMTPClientTransport(SMTPClientTransport transport, int keepAliveIntervalInSec, int keepAliveTimeInSec) {
        this(transport, keepAliveIntervalInSec, keepAliveTimeInSec,  Executors.newScheduledThreadPool(10));
    }
    
    public PooledSMTPClientTransport(SMTPClientTransport transport, int keepAliveIntervalInSec, int keepAliveTimeInSec, ScheduledExecutorService noopSender) {
        if (transport.getDeliveryMode() == SMTPDeliveryMode.STARTTLS_DEPEND || transport.getDeliveryMode() == SMTPDeliveryMode.STARTTLS_TRY) {
            throw new IllegalArgumentException("Pooled of starttls transport is not supported");
        }
        if (keepAliveIntervalInSec < keepAliveTimeInSec) {
            throw new IllegalArgumentException("keepAliveIntervalInSec MUST be < keepAliveTimeInSec");
        }
        this.keepAliveTimeInSec = keepAliveTimeInSec;
        this.transport = transport;
        this.keepAliveIntervalInSec = keepAliveIntervalInSec;
        this.noopSender = noopSender;
    }
    
    
    @Override
    public SMTPDeliveryMode getDeliveryMode() {
        return transport.getDeliveryMode();
    }

    @Override
    public void connect(InetSocketAddress remote, SMTPClientConfig config, final SMTPResponseCallback callback) {
        String key = getKey(remote);
        
        // Get all pooled Sessions for the remote address
        ConcurrentLinkedQueue<PooledSMTPClientSession> sessions = pooledSessions.get(key);
        if (sessions == null) {
            
            // there were no pooled sessions yet. So create a "pool" for it and store it for later usage
            sessions = new ConcurrentLinkedQueue<PooledSMTPClientSession>();
            ConcurrentLinkedQueue<PooledSMTPClientSession> stored = pooledSessions.putIfAbsent(key, sessions);
            if (stored != null) {
                sessions = stored;
            }
        }

        final ConcurrentLinkedQueue<PooledSMTPClientSession> pooledSessions = sessions;

        Iterator<PooledSMTPClientSession> pooledIt = pooledSessions.iterator();
        PooledSMTPClientSession session = null;
        
        // iterate through the pool and check if there is a session left which we can use
        while (pooledIt.hasNext()) {
            session = pooledIt.next();
            if (!session.isInUse() && session.setInUse()) {
                
                // just write the cached welcome response back to the client.
                callback.onResponse(session, (SMTPResponse) session.getAttributes().get(WELCOME_RESPONSE_KEY));
                break;
            } else {
                if (!pooledIt.hasNext()) {
                    // set the session to null if there are no more pooled sessions in place. 
                    // This will make sure that we try to connect to the remote server and get a new one
                    session = null;
                }
            }
        }
        if (session == null) {
            
            // Connect to the remote server as we did not find a usable pooled session
            transport.connect(remote, config, new SMTPResponseCallback() {

                @Override
                public void onResponse(SMTPClientSession session, SMTPResponse response) {
                    PooledSMTPClientSession pooledSession = new PooledSMTPClientSession(session);
                    pooledSession.addCloseListener(new CloseListener() {

                        @Override
                        public void onClose(final SMTPClientSession session) {
                            if (!session.isClosed()) {
                                
                                // check if the session was closed for real. If not just send a RSET to reset it to a reusable state
                                session.send(SMTPRequestImpl.rset(), noopSchedulingResponseCallback);
                            } else {
                                
                                // The session was really closed so remove it from the pooled sessions
                                pooledSessions.remove(session);
                            }
                        }
                    });
                    
                    // store the welcome response for later usage
                    session.getAttributes().put(WELCOME_RESPONSE_KEY, response);
                    callback.onResponse(pooledSession, response);
                    pooledSessions.add(pooledSession);

                }

                @Override
                public void onException(SMTPClientSession session, Throwable t) {
                    callback.onException(session, t);
                }
            });
        }

    }

    @Override
    public void destroy() {
        Iterator<ConcurrentLinkedQueue<PooledSMTPClientSession>> sessionsQueue = pooledSessions.values().iterator();
        while(sessionsQueue.hasNext()) {
            ConcurrentLinkedQueue<PooledSMTPClientSession> sessions = sessionsQueue.next();
            PooledSMTPClientSession session = null;
            while((session = sessions.poll()) != null) {
                session.getWrapped().close();
                session.close();
            }
        }
      
        transport.destroy();
        
    }
    
    
    private String getKey(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
    
    private final class PooledSMTPClientSession implements SMTPClientSession {

        private final SMTPClientSession session;
        private final AtomicBoolean inUse = new AtomicBoolean(true);
        private final List<CloseListener> cListeners = new ArrayList<CloseListener>();
        private final AtomicLong lastSent = new AtomicLong(System.currentTimeMillis());

        public PooledSMTPClientSession(SMTPClientSession session) {
            this.session = session;
        }
        
        
        public boolean setInUse() {
            return inUse.compareAndSet(false, true);
        }
        
        public boolean isInUse() {
            return inUse.get();
        }
        
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
        
        public long getLastSent() {
            return lastSent.get();
        }
        
        public SMTPClientSession getWrapped() {
            return session;
        }
    }
    
    private final static class PooledCallback implements SMTPResponseCallback {
        private SMTPResponseCallback callback;
        private PooledSMTPClientSession pooledSession;

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
