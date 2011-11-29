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
package me.normanmaurer.niosmtp.delivery.impl;

import java.util.Iterator;

import me.normanmaurer.niosmtp.delivery.DeliveryRecipientStatus;
import me.normanmaurer.niosmtp.transport.FutureResult;

/**
 * Simple {@link FutureResult} implementation which holds an {@link Iterator} with all the {@link DeliveryRecipientStatus} instaces
 * 
 * 
 * @author Norman Maurer
 *
 */
public class DeliveryResultImpl extends FutureResult<Iterator<DeliveryRecipientStatus>>{

    public DeliveryResultImpl(Iterable<DeliveryRecipientStatus> status) {
        super(null);
        this.status = status;
    }

    private final Iterable<DeliveryRecipientStatus> status;

    @Override
    public Iterator<DeliveryRecipientStatus> getResult() {
        if (status == null) {
            return null;
        }
        return status.iterator();
    }
    

}
