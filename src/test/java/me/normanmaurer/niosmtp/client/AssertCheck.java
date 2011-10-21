/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package me.normanmaurer.niosmtp.client;

import java.util.Iterator;

/**
 * Callback which should be called when a {@link SMTPClientFuture} was received. 
 * 
 * 
 * @author Norman Maurer
 *
 */
public abstract class AssertCheck {
    
    /**
     * Take the {@link SMTPClientFuture} and bass the {@link DeliveryResult}' to the {@link #onDeliveryResult(Iterator)} method.
     * 
     * This implementation does this by called {@link SMTPClientFuture#get()} and so blocks until its ready. 
     * 
     * 
     * @param future
     * @throws Exception
     */
    public void onSMTPClientFuture(SMTPClientFuture future) throws Exception {
        onDeliveryResult(future.get()); 
    }
    
    /**
     * Callback which will be called once the {@link SMTPClientFuture#isDone()} is true
     * 
     * Assert checks should be performed here
     * 
     * @param result
     */
    protected abstract void onDeliveryResult(Iterator<DeliveryResult> result);
}
