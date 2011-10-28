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

import me.normanmaurer.niosmtp.SMTPByteArrayMessage;

/**
 * {@link SMTPByteArrayMessage} implementation which just use given <code>byte</code> arrays
 * 
 * @author Norman Maurer
 *
 */
public class SMTPByteArrayMessageImpl extends SMTPByteArrayMessage{

    private final byte[] _7BitMessage;

    private final byte[] _8BitMessage;

    /**
     * Create a {@link SMTPByteArrayMessageImpl} instance which use the same <code>byte</code> array for {@link #get7BitAsByteArray()} and {@link #get7BitAsByteArray()}
     * 
     * @param message
     */
    public SMTPByteArrayMessageImpl(final byte[] message) {
        this._7BitMessage = message;
        this._8BitMessage = message;

    }
    
    /**
     * Create a {@link SMTPByteArrayMessageImpl} instance which uses the given <code>byte</code> arrays for {@link #get7BitAsByteArray()} and {@link #get8BitAsByteArray()}
     * 
     * @param _7BitMessage
     * @param _8BitMessage
     */
    public SMTPByteArrayMessageImpl(final byte[] _7BitMessage, final byte[] _8BitMessage) {
        this._7BitMessage = _7BitMessage;
        this._8BitMessage = _8BitMessage;

    }
    
    @Override
    public byte[] get7BitAsByteArray() {
        return _7BitMessage;
    }

    @Override
    public byte[] get8BitAsByteArray() {
        return _8BitMessage;
    }
    
    

}
