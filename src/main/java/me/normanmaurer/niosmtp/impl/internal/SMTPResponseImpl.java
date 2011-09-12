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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPResponse;

class SMTPResponseImpl implements SMTPResponse{

    private int code;
    private final List<String> lines = new ArrayList<String>();

    public SMTPResponseImpl(int code) {
        this.code = code;
    }
    @Override
    public int getCode() {
        return code;
    }

    public void addLine(String line) {
        lines.add(line);
    }

    @Override
    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public String toString() {
        Iterator<String> it = lines.iterator();

        if (it.hasNext()) {
            StringBuilder sb = new StringBuilder();
            while(it.hasNext()) {
                String line = it.next();
                boolean hasNext = it.hasNext();
                sb.append(getCode());
                if (hasNext) {
                    sb.append("-");
                } else {
                    sb.append(" ");
                }
                sb.append(line);
                if (hasNext) {
                    sb.append("\r\n");
                }
                
            }
            return sb.toString();
        }
        return getCode() + "";
        
    }
}
