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
package me.normanmaurer.niosmtp.impl;

import java.io.IOException;
import java.io.InputStream;

import me.normanmaurer.niosmtp.MessageInput;

/**
 * {@link MessageInput} implementation does return 7bit message even if the remote SMTP Server supports 8BITMIME
 * 
 * 
 * @author Norman Maurer
 *
 */
public class SimpleMessageInput implements MessageInput{


    
    private InputStream in;

    /**
     * The given {@link InputStream} must contain a message in 7bit 
     * 
     * @param in
     */
    public SimpleMessageInput(InputStream in) {
        this.in = in;
    }

    /**
     * Throws {@link IOException} as conversion is not supported
     */
    @Override
    public InputStream get7bit() throws IOException {
        return in;
    }


    /**
     * Just calls {@link #get7bit()} as we don't do any optimization for 8BITMIME
     */
    @Override
    public InputStream get8Bit() throws IOException {
        return get7bit();
    }

}
