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

import java.util.List;

/**
 * The SMTP Response which is send back from the SMTP Server to the client
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPResponse {
    
    /**
     * Return the return code
     * 
     * @return code
     */
    public int getCode();

    /**
     * Return all lines of the response as an unmodifiable {@link List}. This may be null
     * 
     * @return lines
     */
    public List<String> getLines();
}
