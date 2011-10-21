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
package me.normanmaurer.niosmtp.delivery;

import java.util.Iterator;
import java.util.concurrent.Future;

import me.normanmaurer.niosmtp.transport.SMTPClientSession;


/**
 * A {@link Future} which allows to register {@link SMTPDeliveryFutureListener} and also make it possible to
 * access the {@link DeliveryResult} objects in a blocking fashion.
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPDeliveryFuture extends Future<Iterator<DeliveryResult>>{

    /**
     * Try to get the {@link DeliveryResult}'s without blocking. So if the {@link SMTPDeliveryFuture#isDone()} returns <code>false</code> it will just return null
     * 
     * @return result
     */
    Iterator<DeliveryResult> getNoWait();
    
    
    /**
     * Return the {@link SMTPClientSession} which belongs to the {@link SMTPDeliveryFuture}. This may return null in some cases
     * 
     * @return session
     */
    SMTPClientSession getSession();
    
    
    /**
     * Add the {@link SMTPDeliveryFutureListener} which will notified one the delivery is complete.
     * 
     * If the {@link SMTPDeliveryFuture} was already completed it will notify the {@link SMTPDeliveryFutureListener} directly
     * 
     * 
     * @param listener
     */
    void addListener(SMTPDeliveryFutureListener listener);
    
    /**
     * Remove the {@link SMTPDeliveryFutureListener}
     * 
     * @param listener
     */
    void removeListener(SMTPDeliveryFutureListener listener);
    
    
    /**
     * Return all {@link SMTPDeliveryFutureListener}'s which are registered 
     * 
     * @return listeners
     */
    Iterator<SMTPDeliveryFutureListener> getListeners();
       

}
