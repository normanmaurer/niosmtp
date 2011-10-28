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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Abstract base implementation of {@link SMTPMessage} which allows to access the raw <code>byte</code> array of the {@link SMTPMessage}. This 
 * should only be used if you are sure that the whole {@link SMTPMessage} fit into memory. 
 * 
 * The {@link #get7BitAsByteArray()} and {@link #get8BitAsByteArray()} methods can be used by the Transport implementation for better performing
 * write operations.
 * 
 * 
 * @author Norman Maurer
 *
 */
public abstract class SMTPByteArrayMessage implements SMTPMessage{

    private InputStream _7bitIn;
    private InputStream _8bitIn;
    
    
    @Override
    public InputStream get7bit() throws IOException {
        if (_7bitIn == null) {
            _7bitIn = new ByteArrayInputStream(get7BitAsByteArray());
        }
        return _7bitIn;
    }

    @Override
    public InputStream get8Bit() throws IOException {
        if (_8bitIn == null) {
            _8bitIn = new ByteArrayInputStream(get8BitAsByteArray());
        }
        return _8bitIn;
    }
    
    /**
     * Return the <code>byte</code> array which should be used for the 7Bit message input.
     * 
     * @return 7bit
     */
    public abstract byte[] get7BitAsByteArray();

    /**
     * Return the <code>byte</code> array which should be used for the 8Bit message input.
     * 
     * @return 8bit
     */
    public abstract byte[] get8BitAsByteArray();

}
