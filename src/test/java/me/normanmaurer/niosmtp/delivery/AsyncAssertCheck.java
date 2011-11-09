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
package me.normanmaurer.niosmtp.delivery;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.transport.FutureResult;

/**
 * Execute the wrapped {@link AssertCheck} in an asynchronous way by using a {@link SMTPClientFutureListener}
 * 
 * @author Norman Maurer
 *
 */
public class AsyncAssertCheck extends AssertCheck{
    
    private final AssertCheck check;

    public AsyncAssertCheck(final AssertCheck check) {
        this.check = check;
    }
    
    /**
     * Register an {@link SMTPClientFutureListener} to the given {@link SMTPClientFuture} to call {@link #onDeliveryResult(Iterator)} once the {@link SMTPClientFutureListener#operationComplete(Iterator)}
     * is called
     * 
     */
    @Override
    public void onSMTPClientFuture(SMTPClientFuture<Collection<FutureResult<Iterator<DeliveryRecipientStatus>>>> future) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        future.addListener(new SMTPClientFutureListener<Collection<FutureResult<Iterator<DeliveryRecipientStatus>>>>() {
            
            @Override
            public void operationComplete(SMTPClientFuture<Collection<FutureResult<Iterator<DeliveryRecipientStatus>>>> future) {
                Iterator<FutureResult<Iterator<DeliveryRecipientStatus>>> result = future.getNoWait().iterator();

                onDeliveryResult(result);
                latch.countDown();
            }
        });
        latch.await();
    }

    @Override
    protected void onDeliveryResult(Iterator<FutureResult<Iterator<DeliveryRecipientStatus>>> result) {
        check.onDeliveryResult(result);
    }
}
