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
import java.io.PushbackInputStream;


/**
 * {@link InputStream} which takes care of correctly terminating the DATA command. This is done by append a CRLF.CRLF to wrapped
 * {@link InputStream} if needed.
 * 
 * This {@link InputStream} also does the dot-stuffing as stated in the SMTP-spec
 * 
 * @author Norman Maurer
 *
 */
public class DataTerminatingInputStream extends InputStream {

    private int last;
    private byte[] extraData;
    private int pos = 0;
    boolean startLine = true;
    private boolean endOfStream = false;
    private PushbackInputStream in;

    public DataTerminatingInputStream(InputStream in) {
        this.in = new PushbackInputStream(in, 2);
        startLine = true;

    }

    @Override
    public int read() throws IOException {
        
        PushbackInputStream pin = (PushbackInputStream) in;
        int i = pin.read();
        if (startLine && endOfStream == false) {
            startLine = false;
            if (i == '.') {
                pin.unread(i);
                return '.';
            }
            
        }
       
        if (last == '\r' && i == '\n') {
            startLine = true;
        }
        
        if (endOfStream == false) {
            if (i == -1) {
                endOfStream = true;
                calculateExtraData();
                return readNext();
            } else {
                last = i;
            }
            return i;

        } else {
            return readNext();
        }
                
       
    }

    private void calculateExtraData() {
        if (last == '\n') {
            extraData = new byte[3];
            extraData[0] = '.';
            extraData[1] = '\r';
            extraData[2] = '\n';        
        } else if (last == '\r') {
            extraData = new byte[4];
            extraData[0] = '\n';
            extraData[1] = '.';
            extraData[2] = '\r';
            extraData[3] = '\n';

        } else {
            extraData = new byte[5];
            extraData[0] = '\r';
            extraData[1] = '\n';
            extraData[2] = '.';
            extraData[3] = '\r';
            extraData[4] = '\n';
        }

    }

    private int readNext() {
        if (extraData == null || extraData.length == pos) {
            return -1;
        } else {
            return extraData[pos++];
        }
    }



    @Override
    public int available() throws IOException {
        if (endOfStream) {
            return extraData.length - pos;
        } else {
            return in.available();
        }
    }





    @Override
    public void close() throws IOException {
        in.close();
    }





    @Override
    public  void mark(int readlimit) {
    }





    @Override
    public void reset() throws IOException {
        throw new IOException("Not supported");
    }


    @Override
    public boolean markSupported() {
        return false;
    }
    
    
}
