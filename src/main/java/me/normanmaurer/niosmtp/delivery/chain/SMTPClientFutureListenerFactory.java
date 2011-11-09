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
package me.normanmaurer.niosmtp.delivery.chain;


import java.util.Collection;

import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPPipeliningRequest;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * A Factory which is responsible for return the right {@link SMTPClientFutureListener} depending on the state of the {@link SMTPSession} and the {@link SMTPRequest} or 
 * {@link SMTPMessage}
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientFutureListenerFactory {

    /**
     * Return the {@link SMTPClientFutureListener} to use after the welcome {@link SMTPResponse} will get received
     * 
     * @param session
     * @return callback
     * @throws SMTPException
     */
    public SMTPClientFutureListener<FutureResult<SMTPResponse>> getListener(SMTPClientSession session) throws SMTPException;
    
    /**
     * Return the {@link SMTPClientFutureListener} for the given {@link SMTPSession} and {@link SMTPRequest}
     * 
     * @param session
     * @param request
     * @return callback
     * @throws SMTPException
     */
    public SMTPClientFutureListener<FutureResult<SMTPResponse>> getListener(SMTPClientSession session, SMTPRequest request) throws SMTPException;
    
    
    public SMTPClientFutureListener<FutureResult<Collection<SMTPResponse>>> getListener(SMTPClientSession session, SMTPPipeliningRequest request) throws SMTPException;

    
    /**
     * Return the {@link SMTPClientFutureListener} for the given {@link SMTPSession} and {@link SMTPMessage}
     * 
     * @param session
     * @param input
     * @return callback
     * @throws SMTPException
     */
    public SMTPClientFutureListener<FutureResult<Collection<SMTPResponse>>> getListener(SMTPClientSession session, SMTPMessage input) throws SMTPException;
}
