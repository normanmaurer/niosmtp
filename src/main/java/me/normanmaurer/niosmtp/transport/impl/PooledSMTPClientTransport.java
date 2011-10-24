package me.normanmaurer.niosmtp.transport.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPResponseImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

public class PooledSMTPClientTransport implements SMTPClientTransport{

    private final static String WELCOME_RESPONSE_KEY = "WELCOME_RESPONSE";
    
    private final static SMTPResponse QUIT_RESPONSE = new SMTPResponseImpl(221);
    private final int keepAliveTimeInSec;
    private final SMTPClientTransport transport;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<PooledSMTPClientSession>> pooledSessions = new ConcurrentHashMap<String, ConcurrentLinkedQueue<PooledSMTPClientSession>>();
    
    public PooledSMTPClientTransport(SMTPClientTransport transport, int keepAliveTimeInSec) {
        this.keepAliveTimeInSec = keepAliveTimeInSec;
        this.transport = transport;
    }
    
    
    @Override
    public SMTPDeliveryMode getDeliveryMode() {
        return transport.getDeliveryMode();
    }

    @Override
    public void connect(InetSocketAddress remote, SMTPClientConfig config, SMTPResponseCallback callback) {
        String key = getKey(remote);
        ConcurrentLinkedQueue<PooledSMTPClientSession> sessions = pooledSessions.get(key);
        if (sessions == null) {
            sessions = new ConcurrentLinkedQueue<PooledSMTPClientSession>();
            ConcurrentLinkedQueue<PooledSMTPClientSession> stored = pooledSessions.putIfAbsent(key, sessions);
            if (stored != null) {
                sessions = stored;
            }
        }
        
       Iterator<PooledSMTPClientSession> pooledIt = sessions.iterator();
       
       PooledSMTPClientSession session = null;
       while(pooledIt.hasNext()) {
           session = pooledIt.next();
           if (!session.isInUse() && session.setInUse()) {
               callback.onResponse(session, (SMTPResponse)session.getAttributes().get(WELCOME_RESPONSE_KEY));
               break;
           }
       }
       if (session == null) {
           transport.connect(remote, config, callback);
       }
       
    }

    
    @Override
    public void destroy() {
        Iterator<ConcurrentLinkedQueue<PooledSMTPClientSession>> sessionsQueue = pooledSessions.values().iterator();
        while(sessionsQueue.hasNext()) {
            ConcurrentLinkedQueue<PooledSMTPClientSession> sessions = sessionsQueue.next();
            SMTPClientSession session = null;
            while((session = sessions.poll()) != null) {
                session.close();
            }
        }
      
        transport.destroy();
        
    }
    
    
    private String getKey(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
    private final class PooledSMTPClientSession implements SMTPClientSession {

        private SMTPClientSession session;
        private AtomicBoolean inUse = new AtomicBoolean(true);
        private List<CloseListener> cListeners = new ArrayList<CloseListener>();
        
        public PooledSMTPClientSession(SMTPClientSession session, final ConcurrentLinkedQueue<PooledSMTPClientSession> sessions) {
            this.session = session;
            session.addCloseListener(new CloseListener() {
                
                @Override
                public void onClose(SMTPClientSession session) {
                    sessions.remove(PooledSMTPClientSession.this);
                }
            });
        }
        
        
        public boolean setInUse() {
            return this.inUse.compareAndSet(false, true);
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

        @Override
        public void send(SMTPRequest request, SMTPResponseCallback callback) {
            if (request.getCommand().equalsIgnoreCase("QUIT")) {
                callback.onResponse(this, QUIT_RESPONSE);
                
                setInUse();
            } else {
                session.send(request, callback);
            }
        }

        @Override
        public void send(MessageInput request, SMTPResponseCallback callback) {
            session.send(request, callback);
            
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
    }

}
