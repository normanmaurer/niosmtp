/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* Selene licenses this file to You under the Apache License, Version 2.0
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

/**
 * A {@link Future} which allows to register {@link SMTPClientFutureListener} and also make it possible to
 * access the {@link DeliveryResult} objects in a blocking fashion.
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientFuture extends Future<DeliveryResult>{

    /**
     * Add the {@link SMTPClientFutureListener} which will notified one the delivery is complete.
     * 
     * If the {@link SMTPClientFuture} was already completed it will notify the {@link SMTPClientFutureListener} directly
     * 
     * 
     * @param listener
     */
    public void addListener(SMTPClientFutureListener listener);
    
    /**
     * Remove the {@link SMTPClientFutureListener}
     * 
     * @param listener
     */
    public void removeListener(SMTPClientFutureListener listener);
}
