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
package me.normanmaurer.niosmtp.impl.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPPipelinedRequest;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.core.SMTPClientConstants;
import me.normanmaurer.niosmtp.core.StringUtils;

/**
 * Simple {@link SMTPPipelinedRequest} implementation which use a {@link ArrayList} to store all {@link SMTPRequest}'s.
 * 
 * Be aware that {@link #getPipelinedRequests()} will return the {@link SMTPRequest}'s in the same order as they were added via 
 * {@link #add(SMTPRequest)} before. So you must take care of submitting them in a valid order by your self
 * 
 * @author Norman Maurer
 *
 */
public class SMTPPipelinedRequestImpl implements SMTPPipelinedRequest, SMTPClientConstants{
    private List<SMTPRequest> requests = new ArrayList<SMTPRequest>();
    
    @Override
    public Iterator<SMTPRequest> getPipelinedRequests() {
        return requests.iterator();
    }
    
    /**
     * Add the {@link SMTPRequest}
     * 
     * 
     * @param request
     */
    public void add(SMTPRequest request) {
        this.requests.add(request);
    }
    
    @Override
    public String toString() {
        return StringUtils.toString(this);
    }
}
