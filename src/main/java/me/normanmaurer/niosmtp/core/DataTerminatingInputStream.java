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
    private final PushbackInputStream in;
    private boolean empty = true;
    private final static byte CR = '\r';
    private final static byte LF = '\n';
    private final static byte DOT = '.';
    private final static byte[] DOT_CRLF = new byte[] {DOT, CR, LF};
    private final static byte[] CRLF_DOT_CRLF = new byte[] {CR, LF, DOT, CR, LF};
    private final static byte[] LF_DOT_CRLF = new byte[] {LF, DOT, CR, LF};
    
    
    public DataTerminatingInputStream(InputStream in) {
        this.in = new PushbackInputStream(in, 2);
        startLine = true;

    }

    @Override
    public int read() throws IOException {
        int i = in.read();
        if (empty && i != -1) {
            empty = false;
        }
        
        if (startLine && endOfStream == false) {
            startLine = false;
            if (i == DOT) {
                in.unread(i);
                return DOT;
            }
            
        }
       
        if (last == CR && i == LF) {
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
        if (empty || last == LF) {
            extraData = DOT_CRLF;     
        } else if (last == CR) {
            extraData = LF_DOT_CRLF;
        } else {
            extraData = CRLF_DOT_CRLF;
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
