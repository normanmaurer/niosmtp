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
package me.normanmaurer.niosmtp;

import java.util.concurrent.Future;

import me.normanmaurer.niosmtp.transport.SMTPClientSession;


/**
 * A {@link Future} which allows to register {@link SMTPClientFutureListener} and also make it possible to
 * access the contained objects in a blocking fashion.
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientFuture<E> extends Future<E>{

    /**
     * Try to get <code>E</code> without blocking. So if the {@link SMTPClientFuture#isDone()} returns <code>false</code> it will just return null
     * 
     * @return result
     */
    E getNoWait();
    
    
    /**
     * Return the {@link SMTPClientSession} which belongs to the {@link SMTPClientFuture}. This may return null in some cases
     * 
     * @return session
     */
    SMTPClientSession getSession();
    
    
    /**
     * Add the {@link SMTPClientFutureListener} which will notified once the {@link SMTPClientFuture} is complete.
     * 
     * If the {@link SMTPClientFuture} was already completed it will notify the {@link SMTPClientFutureListener} directly
     * 
     * 
     * @param listener
     */
    void addListener(SMTPClientFutureListener<E> listener);
    
    /**
     * Remove the {@link SMTPClientFutureListener}
     * 
     * @param listener
     */
    void removeListener(SMTPClientFutureListener<E> listener);

}
