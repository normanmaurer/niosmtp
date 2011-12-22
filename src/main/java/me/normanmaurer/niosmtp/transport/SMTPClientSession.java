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
import java.util.Set;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPMessageSubmit;
import me.normanmaurer.niosmtp.SMTPPipeliningRequest;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.FutureResult.Void;

import org.slf4j.Logger;

/**
 * This is the SMTP Session which belongs to a Connection to a SMTP Server. It's created once a connection was successfully established and
 * valid till its closed via {@link SMTPClientSession#close()} or an {@link Exception} 
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientSession {

    /**
     * Return the {@link SMTPDeliveryMode} which is used for the underlying transport
     * 
     * @return mode
     */
    SMTPDeliveryMode getDeliveryMode();
    
    /**
     * Allow to store a object to the {@link SMTPClientSession} with the given key. To remove a previous stored object just use a <code>null</code> value with the given key.
     * 
     * @param key the key under which the given value should be stored
     * @param value the value which should be stored under the given key or null if the previous stored value should just be removed
     * @return storedValue the previous stored value or null if no value was stored before with the key
     */
    Object setAttribute(String key, Object value);
    
    /**
     * Return the value which was stored under the key
     * 
     * @param key the key for which the value should be returned
     * @return value the value for the given key or <code>null</code> if no value is stored under the given key
     */
    Object getAttribute(String key);
    
    /**
     * Return a "read-only" {@link Set} of all supported EXTENSIONS. This will be set in the EHLO Response so you will get an empty {@link Set} before the EHLO
     * Response was processed
     * 
     * @return extensions
     */
    Set<String> getSupportedExtensions();
    
    /**
     * Add the supported EXTENSIONS for the {@link SMTPClientSession}. 
     * 
     * @param extension
     */
    void addSupportedExtensions(String extension);
    
    /**
     * Return the id of the {@link SMTPClientSession}.
     * 
     * @return id
     */
    String getId();
    
    /**
     * Return the {@link Logger} which belongs the {@link SMTPClientSession}
     * 
     * @return logger
     */
    Logger getLogger();
    
    /**
     * Return <code>true</code> if the {@link SMTPClientSession} is encrypted
     * 
     * @return encrypted
     */
    boolean isEncrypted();
    
    /**
     * Start TLS encryption
     * 
     * @return future
     */
    SMTPClientFuture<FutureResult<Void>> startTLS();
        
    /**
     * Send the given {@link SMTPRequest} to the connected SMTP-Server. 
     * 
     * @param request
     * @return future
     */
    SMTPClientFuture<FutureResult<SMTPResponse>> send(SMTPRequest request);
    
    /**
     * Send the given {@link SMTPPipeliningRequest} to the connected SMTP-Server.
     * 
     * @param request
     * @return future
     */
    SMTPClientFuture<FutureResult<Collection<SMTPResponse>>> send(SMTPPipeliningRequest request);

    
    /**
     * Send the given {@link SMTPMessageSubmit} to the connected SMTP-Server.
     * 
     * @param request
     * @return future
     */
    SMTPClientFuture<FutureResult<Collection<SMTPResponse>>> send(SMTPMessageSubmit request);

    /**
     * Close the {@link SMTPClientSession}
     * 
     * @return future
     */
    SMTPClientFuture<FutureResult<Void>> close();
    
    /**
     * Return the {@link SMTPClientFuture} which will get notified once the {@link SMTPClientSession} was closed
     * 
     * @return future
     */
    SMTPClientFuture<FutureResult<Void>> getCloseFuture();
    
    /**
     * Return <code>true</code> if the {@link SMTPClientSession} is closed (disconnected)
     * 
     * @return closed
     */
    boolean isClosed();
    
    
    /**
     * Return the configuration which was used to create this {@link SMTPClientSession}
     * 
     * @return config
     */
    SMTPClientConfig getConfig();
    
    /**
     * Return the remote {@link InetSocketAddress} which the {@link SMTPClientSession} is bound to. If the {@link SMTPClientSession} is not yet bound this may return <code>null</code>
     * 
     * @return remoteAddress
     */
    InetSocketAddress getRemoteAddress();
    
    
    
    /**
     * Return the local {@link InetSocketAddress} which the {@link SMTPClientSession} is bound to. If the {@link SMTPClientSession} is not yet bound this may return <code>null</code>
     * 
     * @return localAddress
     */
    InetSocketAddress getLocalAddress();

}
