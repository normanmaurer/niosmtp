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
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPConnectionException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.delivery.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.AbstractSMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

/**
 * {@link SMTPClientTransport} implementation which allows to wrap another {@link SMTPClientTransport} and act as connection limiter.
 * 
 * If the {@link #connect(InetSocketAddress, SMTPClientConfig, SMTPResponseCallback)} method is called and the max configured connections are reached its just
 * queued and dispatched once another connection was closed.
 * 
 * If a the configured queue size is reached this implementation will call the {@link SMTPResponseCallback#onException(SMTPClientSession, Throwable) method to notify
 * the callback about the problems
 * 
 * @author Norman Maurer
 *
 */
public class LimitingSMTPClientTransport implements SMTPClientTransport {

    private final Logger logger = LoggerFactory.getLogger(LimitingSMTPClientTransport.class);
    
    private final static SMTPConnectionException CONNECTION_EXCEPTION = new SMTPConnectionException("Unable to connect before SMTPClientTransport was destroyed");
    private final static SMTPConnectionException QUEUE_LIMIT_REACHED_EXCEPTION = new SMTPConnectionException("To many queued connection attempts");
    private final static SMTPConnectionException NOT_CONNECTED_EXCEPTION = new SMTPConnectionException("Not connected");

    private final SMTPClientTransport transport;
    private final int connectionLimit;
    private final SMTPClientFutureListener<FutureResult<Boolean>> closeListener = new ReleaseConnectionCloseHandler();
    private final AtomicInteger connectionCount = new AtomicInteger(0);
    private final ConcurrentLinkedQueue<QueuedConnectRequest> connectionQueue = new ConcurrentLinkedQueue<QueuedConnectRequest>();
    private final int maxQueuedConnectionLimit;
    
    /**
     * Create a new {@link LimitingSMTPClientTransport} which wraps another {@link SMTPClientTransport}
     * 
     * 
     * @param transport the {@link SMTPClientTransport} which should be limited
     * @param connectionLimit the connectionLimit to use. This value must be >= 1
     * @param maxQueuedConnectionLimit the maximal number of queued connection requests. If the limit is reached and a new request is made via the {@link #connect(InetSocketAddress, SMTPClientConfig, SMTPResponseCallback)} method,
     *                                 this implementation takes care of calling the {@link SMTPResponseCallback#onException(SMTPClientSession, Throwable)} method. 
     *                                 Use -1 to make the queue unlimited
     */
    public LimitingSMTPClientTransport(SMTPClientTransport transport, int connectionLimit, int maxQueuedConnectionLimit)  {
        this.transport = transport;
        if (connectionLimit < 1) {
            throw new IllegalArgumentException("connectionLimit must be >= 1");
        }
        this.connectionLimit = connectionLimit;
        this.maxQueuedConnectionLimit = maxQueuedConnectionLimit;
    }
    
    /**
     * Return the maximal queued connection limit. After this is hit every new {@link #connect(InetSocketAddress, SMTPClientConfig, SMTPResponseCallback)} attempt will trigger an {@link SMTPResponseCallback#onException(SMTPClientSession, Throwable)} call.
     * 
     * For unlimited -1 is used
     * 
     * @return maxQueuedLimit
     */
    public int getMaxQueuedConnectionLimit() {
        return maxQueuedConnectionLimit;
    }
    
    /**
     * Return the current active connection count
     * 
     * @return count
     */
    public int getConnectionCount() {
        return connectionCount.get();
    }
    
    /**
     * Return the configured connection limit. If the limit is hit the request is queued for later execution (as soon as possible).
     * 
     * @return connLimit
     */
    public int getConnectionLimit() {
        return connectionLimit;
    }
    
    
    @Override
    public SMTPDeliveryMode getDeliveryMode() {
        return transport.getDeliveryMode();
    }

    
    @Override
    public SMTPClientFuture<FutureResult<SMTPResponse>> connect(InetSocketAddress remote, SMTPClientConfig config) {
        return null;
        /*
        if (connectionCount.incrementAndGet() > connectionLimit)  {
            connectionCount.decrementAndGet();
            if (maxQueuedConnectionLimit > -1 && connectionQueue.size() +1 > maxQueuedConnectionLimit) {
                callback.onException(new UnconnectedSMTPClientSession(logger, config, getDeliveryMode()), QUEUE_LIMIT_REACHED_EXCEPTION);
            } else {
                connectionQueue.add(new QueuedConnectRequest(remote, config, callback));
            }
        } else {
            connectToServer(remote, config, callback);
        }
        */
        
    }

    private SMTPClientFuture<FutureResult<SMTPResponse>> connectToServer(InetSocketAddress remote, SMTPClientConfig config) {
        SMTPClientFuture<FutureResult<SMTPResponse>> future = transport.connect(remote, config);
        future.addListener(new SMTPClientFutureListener<FutureResult<SMTPResponse>>() {

            @Override
            public void operationComplete(SMTPClientFuture<FutureResult<SMTPResponse>> future) {
                future.getSession().getCloseFuture().addListener(closeListener);
            }
        });
        
        return future;
    }
    
    
    @Override
    public void destroy() {        
        // loop over all queued connection requests and fail them all
        QueuedConnectRequest request = null;
        while((request = connectionQueue.poll()) != null) {
        //    request.callback.onException(new UnconnectedSMTPClientSession(logger, request.config, getDeliveryMode()), CONNECTION_EXCEPTION);
        }
        transport.destroy();

    }
    
    private final static class QueuedConnectRequest {
        private final InetSocketAddress address;
        private final SMTPClientConfig config;
        private final SMTPClientFutureListener<FutureResult<SMTPResponse>> listener;

        public QueuedConnectRequest(InetSocketAddress address, SMTPClientConfig config, SMTPClientFutureListener<FutureResult<SMTPResponse>> listener) {
            this.address = address;
            this.config = config;
            this.listener = listener;
        }
        
    }
    
    /**
     * {@link CloseListener} which will dispatch the next queued connection request as soon as the {@link SMTPClientSession} was closed
     * 
     * @author Norman Maurer
     *
     */
    private final class ReleaseConnectionCloseHandler implements SMTPClientFutureListener<FutureResult<Boolean>> {


        @Override
        public void operationComplete(SMTPClientFuture<FutureResult<Boolean>> future) {
            if(connectionCount.decrementAndGet() <= connectionLimit) {
                
                while(connectionCount.incrementAndGet() <= connectionLimit) {
                    QueuedConnectRequest request = connectionQueue.poll();
                    if (request != null) {
                        SMTPClientFuture<FutureResult<SMTPResponse>> cFuture = connectToServer(request.address, request.config);
                        cFuture.addListener(request.listener);
                    } else {
                        connectionCount.decrementAndGet();
                        break;
                    }
                }
                connectionCount.decrementAndGet();
            }            
        }
        
    }
/*
    private class UnconnectedSMTPClientSession extends AbstractSMTPClientSession {

        private final String id = UUID.randomUUID().toString();
        private final Collection<CloseListener> listeners = new CopyOnWriteArrayList<CloseListener>();
        public UnconnectedSMTPClientSession(Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode) {
            super(logger, config, mode, null, null);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public void startTLS() {
            // Do nothing
        }

        @Override
        public void send(SMTPRequest request, SMTPResponseCallback callback) {
            callback.onException(UnconnectedSMTPClientSession.this, NOT_CONNECTED_EXCEPTION);
        }

        @Override
        public void send(SMTPMessage request, SMTPResponseCallback callback) {
            callback.onException(UnconnectedSMTPClientSession.this, NOT_CONNECTED_EXCEPTION);
        }

        @Override
        public void close() {
            // do nothing
        }

        @Override
        public boolean isClosed() {
            return true;
        }

        @Override
        public void addCloseListener(CloseListener listener) {
            listeners.add(closeListener);
            listener.onClose(UnconnectedSMTPClientSession.this);

        }

        @Override
        public void removeCloseListener(CloseListener listener) {
            listeners.remove(listener);
            
        }

        @Override
        public Iterator<CloseListener> getCloseListeners() {
            return listeners.iterator();
        }
        
    }
    */

}
