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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPPipeliningRequest;
import me.normanmaurer.niosmtp.SMTPRequest;

/**
 * POJO implementation of {@link SMTPPipeliningRequest} 
 * 
 * @author Norman Maurer
 *
 */
public class SMTPPipeliningRequestImpl implements SMTPPipeliningRequest{
    private final Collection<SMTPRequest> requests;
    
    public SMTPPipeliningRequestImpl(String sender, Iterator<String> recipients) {
        List<SMTPRequest> reqs = new ArrayList<SMTPRequest>();
        reqs.add(SMTPRequestImpl.mail(sender));
        while(recipients.hasNext()) {
            reqs.add(SMTPRequestImpl.rcpt(recipients.next()));
        }
        reqs.add(SMTPRequestImpl.data());
        requests = Collections.unmodifiableCollection(reqs);
    }
    
    @Override
    public Collection<SMTPRequest> getRequests() {
        return requests;
    }

    @Override
    public String toString() {
        return StringUtils.toString(this);
    }
}
