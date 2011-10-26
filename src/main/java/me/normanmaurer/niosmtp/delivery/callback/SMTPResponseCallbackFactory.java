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
package me.normanmaurer.niosmtp.delivery.callback;


import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * A Factory which is responsible for return the right {@link SMTPResponseCallback} depending on the state of the {@link SMTPSession} and the {@link SMTPRequest} or 
 * {@link MessageInput}
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPResponseCallbackFactory {

    /**
     * Return the {@link SMTPResponseCallback} to use after the welcome {@link SMTPResponse} will get received
     * 
     * @param session
     * @return callback
     * @throws SMTPException
     */
    public SMTPResponseCallback getCallback(SMTPClientSession session) throws SMTPException;
    
    /**
     * Return the {@link SMTPResponseCallback} for the given {@link SMTPSession} and {@link SMTPRequest}
     * 
     * @param session
     * @param request
     * @return callback
     * @throws SMTPException
     */
    public SMTPResponseCallback getCallback(SMTPClientSession session, SMTPRequest request) throws SMTPException;
    
    /**
     * Return the {@link SMTPResponseCallback} for the given {@link SMTPSession} and {@link MessageInput}
     * 
     * @param session
     * @param input
     * @return callback
     * @throws SMTPException
     */
    public SMTPResponseCallback getCallback(SMTPClientSession session, MessageInput input) throws SMTPException;
}
