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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPResponse;

/**
 * {@link SMTPResponse} implementation which use a {@link ArrayList} to hold response lines
 * 
 * @author Norman Maurer
 * 
 *
 */
public class SMTPResponseImpl implements SMTPResponse{

    private final int code;
    private final List<String> lines = new ArrayList<String>();

    public SMTPResponseImpl(int code) {
        this.code = code;
    }
    @Override
    public int getCode() {
        return code;
    }

    /**
     * Add the given line to the {@link SMTPResponse}
     * 
     * @param line
     */
    public void addLine(String line) {
        lines.add(line);
    }

    @Override
    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public String toString() {
        return StringUtils.toString(this);
    }
}
