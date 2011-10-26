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
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
public class PooledSMTPClientTransport extends LimitingSMTPClientTransport{

    private final static String WELCOME_RESPONSE_KEY = "WELCOME_RESPONSE";    
    private final int keepAliveTimeInSec;
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
                    
                    // make sure no other thread is closing it already
                    if (pooledSession.acquire()) {
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

    
    public PooledSMTPClientTransport(SMTPClientTransport transport, int maxPoolSize, int keepAliveIntervalInSec, int keepAliveTimeInSec) {
        this(transport, maxPoolSize, keepAliveIntervalInSec, keepAliveTimeInSec,  Executors.newScheduledThreadPool(10));
    }
    
    public PooledSMTPClientTransport(SMTPClientTransport transport, int maxPoolSize, int keepAliveIntervalInSec, int keepAliveTimeInSec, ScheduledExecutorService noopSender) {
        super(transport, maxPoolSize, -1);
        if (transport.getDeliveryMode() == SMTPDeliveryMode.STARTTLS_DEPEND || transport.getDeliveryMode() == SMTPDeliveryMode.STARTTLS_TRY) {
            throw new IllegalArgumentException("Pooled of starttls transport is not supported");
        }
        if (keepAliveIntervalInSec < keepAliveTimeInSec) {
            throw new IllegalArgumentException("keepAliveIntervalInSec MUST be < keepAliveTimeInSec");
        }
        this.keepAliveTimeInSec = keepAliveTimeInSec;
        this.keepAliveIntervalInSec = keepAliveIntervalInSec;
        this.noopSender = noopSender;
    }
    
    

    /**
     * Return the maximal pool size
     * 
     * @return maxPoolSize
     */
    public int getMaxPoolSize() {
        return getConnectionLimit();
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

        final ConcurrentLinkedQueue<PooledSMTPClientSession> pSessions = sessions;

        Iterator<PooledSMTPClientSession> pooledIt = pSessions.iterator();
        PooledSMTPClientSession session = null;
        
        // iterate through the pool and check if there is a session left which we can use
        while (pooledIt.hasNext()) {
            session = pooledIt.next();
            if (!session.isInUse() && session.acquire()) {
                
                // just write the cached welcome response back to the client.
                try {
                    callback.onResponse(session, (SMTPResponse) session.getAttributes().get(WELCOME_RESPONSE_KEY));
                } catch (Exception e) {
                    callback.onException(session, e);
                }
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
            super.connect(remote, config, new SMTPResponseCallback() {

                @Override
                public void onResponse(SMTPClientSession session, SMTPResponse response) throws Exception {
                    PooledSMTPClientSession pooledSession = new PooledSMTPClientSession(session);
                    pooledSession.addCloseListener(new CloseListener() {

                        @Override
                        public void onClose(final SMTPClientSession session) {
                            if (!session.isClosed()) {
                                
                                // check if the session was closed for real. If not just send a RSET to reset it to a reusable state
                                session.send(SMTPRequestImpl.rset(), noopSchedulingResponseCallback);
                            } else {
                                
                                // The session was really closed so remove it from the pooled sessions
                                pSessions.remove(session);

                            }
                        }
                    });
                    
                    // store the welcome response for later usage
                    session.getAttributes().put(WELCOME_RESPONSE_KEY, response);
                    callback.onResponse(pooledSession, response);
                    pSessions.add(pooledSession);

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
        noopSender.shutdown();
        
        Iterator<ConcurrentLinkedQueue<PooledSMTPClientSession>> sessionsQueue = pooledSessions.values().iterator();
        while(sessionsQueue.hasNext()) {
            ConcurrentLinkedQueue<PooledSMTPClientSession> sessions = sessionsQueue.next();
            PooledSMTPClientSession session = null;
            while((session = sessions.poll()) != null) {
                session.getWrapped().close();
                session.close();
            }
        }
      
        super.destroy();
        
    }
    
    
    private static String getKey(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
}
