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

import java.io.IOException;
import java.io.InputStream;

/**
 * The {@link MessageInput} implementations offer methods to get the message content. 
 * 
 * 
 * 
 * 
 * @author Norman Maurer
 *
 */
public interface MessageInput {

    /**
     * This method is getting called if the SMTP-Server does not support the 8BITMIME extension. 
     * <br/>
     * <br/>
     * <strong>This method MAY return the same {@link InputStream} on every call, so it should only be used to consume the stream once.</strong>
     * 
     * @return 7bit
     * @throws IOException
     */
    public InputStream get7bit() throws IOException;
    
    
    /**
     * This method is getting called if the SMTP-Server supports the 8BITMIME extension. 
     * <br/>
     * <br/>
     * <strong>This method MAY return the same {@link InputStream} on every call, so it should only be used to consume the stream once.</strong>
     * <br/>
     * <br/>
     * The returned InputStream is not expected to be "raw content" . It cannot include "null" bytes and must be composed of "lines" separated by CRLF and not longer than 998 bytes.
     * 
     * @return convertedMsg
     * @throws IOException
     */
    public InputStream get8Bit() throws IOException;
    
}
