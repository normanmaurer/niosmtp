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
package me.normanmaurer.niosmtp.transport;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;


import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPPipeliningRequest;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;

import org.slf4j.Logger;

/**
 * Abstract base implementation of a {@link SMTPClientSession}
 * 
 * @author Norman Maurer
 *
 */
public abstract class AbstractSMTPClientSession implements SMTPClientSession {


    private final Logger logger;
    private final SMTPDeliveryMode mode;
    private final SMTPClientConfig config;
    private final CopyOnWriteArraySet<String> extensions = new CopyOnWriteArraySet<String>();
    private final ConcurrentMap<String, Object> attrs = new ConcurrentHashMap<String, Object>();
    private final InetSocketAddress remote;
    private final InetSocketAddress local;
    
    public AbstractSMTPClientSession(Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, InetSocketAddress local, InetSocketAddress remote) {
        this.logger = logger;
        this.mode = mode;
        this.config = config;
        this.remote = remote;
        this.local = local;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remote;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return local;
    }

    @Override
    public SMTPDeliveryMode getDeliveryMode() {
        return mode;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attrs;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Collections.unmodifiableSet(extensions);
    }

    @Override
    public void addSupportedExtensions(String extensions) {
        this.extensions.add(extensions);
    }


    @Override
    public Logger getLogger() {
        return logger;
    }

  
    @Override
    public SMTPClientConfig getConfig() {
        return config;
    }

    /**
     * Implementation which just send each {@link SMTPRequest} which is hold in the {@link SMTPPipeliningRequest#getRequests()} method via
     * {@link #send(SMTPRequest, me.normanmaurer.niosmtp.SMTPResponseCallback)} method. 
     * 
     */
    @Override
    public SMTPClientFuture<FutureResult<Collection<SMTPResponse>>> send(SMTPPipeliningRequest request) {
        for(SMTPRequest req: request.getRequests()) {
            send(req);
        }
        return null;
    }
    
    
}
