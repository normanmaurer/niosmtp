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
package me.normanmaurer.niosmtp.transport;

import java.net.ConnectException;

import me.normanmaurer.niosmtp.SMTPConnectionException;
import me.normanmaurer.niosmtp.SMTPException;

public abstract class FutureResult<E> {

   
    private SMTPException exception;

    protected FutureResult(SMTPException exception) {
        this.exception = exception;
    }
    
    
    /**
     * Return true if the processing was successful (no exception)
     * 
     * @return success
     */
    public final boolean isSuccess() {
        return exception == null;
    }    
    /**
     * Return the {@link SMTPException} which was thrown while process the future
     * 
     * @return exception
     */
    
    public final SMTPException getException() {
        return exception;
    }    
    
    /**
     * Return the result. This MAY return null if {@link #getCause()} returns not null
     * 
     * @return status
     */
    public abstract E getResult();
    
    /**
     * Create a new {@link FutureResult} by taking care to wrap or cast the given {@link Throwable} to the right {@link SMTPException}
     * 
     * @param t
     * @return result
     */
    @SuppressWarnings("rawtypes")
    public static FutureResult create(Throwable t) {
        final SMTPException exception;
        if (t instanceof SMTPException) {
            exception = (SMTPException) t;
        } else if (t instanceof ConnectException) {
            exception = new SMTPConnectionException(t);
        } else {
            exception = new SMTPException("Exception while try to deliver msg", t);
        }
        return new FutureResult(exception) {

            @Override
            public Object getResult() {
                return null;
            }
            
        };
        
    }

}
