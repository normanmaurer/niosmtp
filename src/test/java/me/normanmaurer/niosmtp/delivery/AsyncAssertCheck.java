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

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import me.normanmaurer.niosmtp.delivery.DeliveryResult;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryFuture;
import me.normanmaurer.niosmtp.delivery.SMTPDeliveryFutureListener;

/**
 * Execute the wrapped {@link AssertCheck} in an asynchronous way by using a {@link SMTPDeliveryFutureListener}
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
     * Register an {@link SMTPDeliveryFutureListener} to the given {@link SMTPDeliveryFuture} to call {@link #onDeliveryResult(Iterator)} once the {@link SMTPDeliveryFutureListener#operationComplete(Iterator)}
     * is called
     * 
     */
    @Override
    public void onSMTPClientFuture(SMTPDeliveryFuture future) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        future.addListener(new SMTPDeliveryFutureListener() {
            
            @Override
            public void operationComplete(SMTPDeliveryFuture future) {
                Iterator<DeliveryResult> result = future.getNoWait();

                onDeliveryResult(result);
                latch.countDown();
            }
        });
        latch.await();
    }

    @Override
    protected void onDeliveryResult(Iterator<DeliveryResult> result) {
        check.onDeliveryResult(result);
    }
}
