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
package me.normanmaurer.niosmtp.impl.internal;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * {@link FileInputStream} which takes care of correctly terminating the DATA command. This is done by append a CRLF.CRLF to wrapped
 * {@link InputStream} if needed.
 * 
 * @author Norman Maurer
 *
 */
public class DataTerminatingInputStream extends FilterInputStream {

    private int last;
    private byte[] extraData;
    private int pos = 0;
    private boolean complete = false;

    private boolean endOfStream = false;

    public DataTerminatingInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (endOfStream == false) {

            int r = in.read(b, off, len);
            if (r == -1) {
                endOfStream = true;
                calculateExtraData();

                return fillArray(b, off, len);
            } else {
                last = b[off + r - 1];
                return r;
            }
        } else {
            return fillArray(b, off, len);
        }
    }

    private int fillArray(byte[] b, int off, int len) {
        int a = -1;
        int i = 0;
        if (complete) {
            return -1;
        }
        while (i < len) {
            a = readNext();
            if (a == -1) {
                complete = true;
                break;
            } else {
                b[off + i++] = (byte) a;

            }
        }
        return i;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        if (endOfStream == false) {
            int i = super.read();
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
}
