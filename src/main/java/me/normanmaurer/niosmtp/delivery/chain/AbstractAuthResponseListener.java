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
package me.normanmaurer.niosmtp.delivery.chain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * Abstract base class of {@link ChainedSMTPClientFutureListener} which AUTH callbacks should use
 * 
 * @author Norman Maurer
 *
 */
public abstract class AbstractAuthResponseListener extends ChainedSMTPClientFutureListener<SMTPResponse> implements SMTPClientConstants{
    @Override
    protected void setDeliveryStatusForAll(SMTPClientSession session, SMTPResponse response) throws SMTPException {
        // check if the SMTPResponse needs to get decoded 
        if ((response instanceof SMTPAuthResponse) == false) {
            response = new SMTPAuthResponse(response);
        }
        super.setDeliveryStatusForAll(session, response);
    }
    
    protected final class SMTPAuthResponse implements SMTPResponse {
        private SMTPResponse response;

        public SMTPAuthResponse(SMTPResponse response) {
            this.response = response;
        }

        @Override
        public int getCode() {
            return response.getCode();
        }

        @Override
        public List<String> getLines() {
            List<String> lines = response.getLines();
            if (lines != null && !lines.isEmpty()) {
                List<String> decodedLines = new ArrayList<String>();
                // loop over the response lines and decode them as they are base64 encoded.
                for (String line: lines) {
                    decodedLines.add(new String(Base64.decodeBase64(line), CHARSET));
                }
                return decodedLines;
            }
            return lines;
        }
    }

}
