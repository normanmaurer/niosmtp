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

import java.util.Map;
import java.util.Set;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;

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
     * Return a map of attributes which can be used to store data which should be used within the scope of the {@link SMTPClientSession}
     * 
     * @return attributes
     */
    Map<String, Object> getAttributes();
    
    /**
     * Return a {@link Set} of all supported EXTENSIONS. This will be set in the EHLO Response so you will get an empty {@link Set} before the EHLO
     * Response was processed
     * 
     * @return extensions
     */
    Set<String> getSupportedExtensions();
    
    /**
     * Set the supported EXTENSIONS for the {@link SMTPClientSession}. 
     * 
     * @param extensions
     */
    void setSupportedExtensions(Set<String> extensions);

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
     * @return
     */
    boolean isEncrypted();
    
    /**
     * Start TLS encryption
     */
    void startTLS();
        
    /**
     * Send the given {@link SMTPRequest} to the connected SMTP-Server. The given {@link SMTPResponseCallback} will get called
     * once the {@link SMTPResponse} was received or an {@link Exception} was thrown
     * 
     * @param request
     * @param callback
     */
    void send(SMTPRequest request, SMTPResponseCallback callback);
    
    /**
     * Send the given {@link MessageInput} to the connected SMTP-Server. The given {@link SMTPResponseCallback} will get called
     * once the {@link SMTPResponse} was received or an {@link Exception} was thrown
     * 
     * @param request
     * @param callback
     */
    void send(MessageInput request, SMTPResponseCallback callback);

    /**
     * Close the {@link SMTPClientSession}
     */
    void close();
    
    
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
     * Add an {@link CloseListener} which will get notified once the {@link SMTPClientSession} was closed. This may be because the client or the server
     * has closed it
     * 
     * @param listener
     */
    void addCloseListener(CloseListener listener);
    
    /**
     * Listener which will get notified once the {@link SMTPClientSession} was closed
     * 
     * @author Norman Maurer
     *
     */
    public interface CloseListener {
        
        /**
         * Which will get called once the {@link SMTPClientSession} was closed
         * 
         * @param session
         */
        void onClose(SMTPClientSession session);
    }

}
