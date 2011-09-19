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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import junit.framework.Assert;

import me.normanmaurer.niosmtp.core.DataTerminatingInputStream;

public class DataTerminatingInputStreamTest {
    
    private void checkStream(String expected, String msg) throws IOException {
        DataTerminatingInputStream in = new DataTerminatingInputStream(new ByteArrayInputStream(msg.getBytes()));
        try {
            int i = -1;
            int a = 0;
            while ((i = in.read()) != -1) {
                Assert.assertEquals(expected.charAt(a++), (char)i);
            }
            assertEquals(expected.length(), a);

        } finally {
            in.close();
        }
        
    }
    @Test
    public void testNoCRLF() throws IOException {
        String msg = "Subject: test\r\ntest";
        String expected = "Subject: test\r\ntest\r\n.\r\n";

        checkStream(expected, msg);
      
    }

    
    @Test
    public void testNoLF() throws IOException {
        String msg = "Subject: test\r\ntest\r";
        String expected = "Subject: test\r\ntest\r\n.\r\n";

        checkStream(expected, msg);

    }
    
    @Test
    public void testCRLFPresent() throws IOException {
        String msg = "Subject: test\r\ntest\r\n";
        String expected = "Subject: test\r\ntest\r\n.\r\n";

        checkStream(expected, msg);

    }

    
    @Test
    public void testCRLFPresentAndDotStuffingNeeded() throws IOException {
        String msg = "Subject: test\r\n.\r\ntest\r\n";
        String expected = "Subject: test\r\n..\r\ntest\r\n.\r\n";

        checkStream(expected, msg);

    }
}
