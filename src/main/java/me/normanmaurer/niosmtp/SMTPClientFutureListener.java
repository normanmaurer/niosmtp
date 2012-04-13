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


/**
 * A listener which will get informed once the {@link SMTPClientFuture}  was done
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClientFutureListener<E> {

    /**
     * Callback which will get called once the operation was complete
     * 
     * @param future
     */
    void operationComplete(SMTPClientFuture<E> future); 
}
