
package me.normanmaurer.niosmtp.impl.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


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
                // Make sure we respect the offset. Otherwise it could let the RETRCmdHandler
                // hang forever. See JAMES-1222
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
            extraData[1] = '.';
            extraData[2] = '\r';
            extraData[3] = '\n';        
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
