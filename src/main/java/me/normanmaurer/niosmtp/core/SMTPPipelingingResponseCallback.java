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
package me.normanmaurer.niosmtp.core;

import java.util.Iterator;

import me.normanmaurer.niosmtp.SMTPMultiResponseCallback;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

public class SMTPPipelingingResponseCallback implements SMTPMultiResponseCallback{

    private final static String CURRENT_CALLBACK_IN_USE = "CURRENT_CALLBACK_IN_USE";
    
    private Iterator<SMTPResponseCallback> callbacks;

    public SMTPPipelingingResponseCallback(Iterator<SMTPResponseCallback> callbacks) {
        this.callbacks = callbacks;
    }
    
    @Override
    public void onResponse(SMTPClientSession session, SMTPResponse response) throws Exception {
            
        if (callbacks.hasNext()) {
            SMTPResponseCallback callback = null;
            try {
                callback = callbacks.next();
                callback.onResponse(session, response);
            } catch (Exception e) {
                session.getAttributes().put(CURRENT_CALLBACK_IN_USE, callback);
                throw e;
            }
        }
        
        
    }

    @Override
    public void onException(SMTPClientSession session, Throwable t) {
        SMTPResponseCallback callback = (SMTPResponseCallback) session.getAttributes().remove(CURRENT_CALLBACK_IN_USE);
        if (callback == null) {
            if (!isDone(session)) {
                callback = callbacks.next();
            }
        }
        if (callback != null) {
            callback.onException(session, t);
        }

        
        
    }

    @Override
    public boolean isDone(SMTPClientSession session) {
        return !callbacks.hasNext();
    }

}
