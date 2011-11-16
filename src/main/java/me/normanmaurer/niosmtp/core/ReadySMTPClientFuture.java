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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
import java.util.concurrent.TimeoutException;

import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link AbstractSMTPClientFuture} implementation which represent a "ready" future. 
 * 
 * So no blocking will be in place on any method call
 * 
 * @author Norman Maurer
 *
 * @param <E>
 */
public class ReadySMTPClientFuture<E> extends AbstractSMTPClientFuture<E> {

    private final E result;

    public ReadySMTPClientFuture(SMTPClientSession session, E result) {
        this.result = result;
        setSMTPClientSession(session);
    }
    
    
    @Override
    public E getNoWait() {
        return result;
    }

    /**
     * Returns <code>false</code>
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    /**
     * Calls {@link #getNoWait()}
     */
    @Override
    public E get() throws InterruptedException, ExecutionException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return getNoWait();
    }

    
    /**
     * Calls {@link #getNoWait()}
     */
    @Override
    public E get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return getNoWait();
    }

    /**
     * Returns <code>false</code>
     */
    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * Returns <code>true</code>
     */
    @Override
    public boolean isDone() {
        return true;
    }

}
