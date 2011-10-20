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

import java.util.Iterator;

import me.normanmaurer.niosmtp.SMTPClientConstants;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;

/**
 * Utility class which helps to convert objects to <code>String</code>'s
 * 
 * @author Norman Maurer
 *
 */
public class StringUtils implements SMTPClientConstants{

    /**
     * Converts the given {@link SMTPResponse} to a <code>String</code>.  
     * 
     * @param response
     * @return responseAsString
     */
    public static String toString(SMTPResponse response) {
        Iterator<String> it = response.getLines().iterator();

        if (it.hasNext()) {
            StringBuilder sb = new StringBuilder();
            while(it.hasNext()) {
                String line = it.next();
                boolean hasNext = it.hasNext();
                sb.append(response.getCode());
                if (hasNext) {
                    sb.append(SMTPResponse.SEPERATOR);
                } else {
                    sb.append(SMTPResponse.LAST_SEPERATOR);
                }
                sb.append(line);
                if (hasNext) {
                    sb.append(SMTPResponse.LINES_SEPERATOR);
                }
                
            }
            return sb.toString();
        }
        return Integer.toString(response.getCode());
    }
    
    /**
     * Converts the given {@link SMTPRequest} to a <code>String</code>
     * 
     * @param request
     * @return requestAsString
     */
    public static String toString(SMTPRequest request) {
        String argument = request.getArgument();
        String command = request.getCommand();
        if (argument == null) {
            return command;
        } else {
            return command + SMTPRequest.SEPERATOR + argument;
        }
    }
}
