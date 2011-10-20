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

import java.io.IOException;
import java.io.InputStream;

import me.normanmaurer.niosmtp.MessageInput;

/**
 * {@link MessageInput} implementation which use the given {@link InputStream}'s 
 * 
 * 
 * @author Norman Maurer
 *
 */
public class SimpleMessageInput implements MessageInput{


    
    private final InputStream _7BitIn;
    private final InputStream _8BitIn;

    /**
     * Construct a {@link SimpleMessageInput} which use the  given {@link InputStream} for {@link #get7bit()} and {@link #get8Bit()}.
     * </br>
     * </br>
     * The {@link InputStream} <strong>MUST</strong> contain a message in 7bit 
     * 
     * @param message
     */
    public SimpleMessageInput(final InputStream message) {
        this._7BitIn = message;
        this._8BitIn = message;
    }

    /**
     * Construct a {@link SimpleMessageInput} which use the given {@link InputStream}'s 
     * 
     * @param _7BitIn
     * @param _8BitIn
     */
    public SimpleMessageInput(final InputStream _7BitIn, final InputStream _8BitIn) {
        this._7BitIn = _7BitIn;
        this._8BitIn = _8BitIn;
    }
    
    /**
     * Throws {@link IOException} as conversion is not supported
     */
    @Override
    public InputStream get7bit() throws IOException {
        return _7BitIn;
    }


    /**
     * Just calls {@link #get7bit()} as we don't do any optimization for 8BITMIME
     */
    @Override
    public InputStream get8Bit() throws IOException {
        return _8BitIn;
    }

}
