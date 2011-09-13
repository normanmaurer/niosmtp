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

import java.util.Iterator;

/**
 * Allows to send more then one {@link SMTPRequest} in an operation. This is used to optimize PIPELING.
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPPipelinedRequest {

    /**
     * Get all {@link SMTPRequest}'s which should get send in one round-trip
     * 
     * @return requests
     */
    public Iterator<SMTPRequest> getPipelinedRequests();
}
