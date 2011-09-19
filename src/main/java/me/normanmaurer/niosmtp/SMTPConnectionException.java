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

/**
 * {@link SMTPException} which will get thrown if the connection was timed out or refused
 * 
 * @author Norman Maurer
 *
 */
public class SMTPConnectionException extends SMTPException{

    /**
     * 
     */
    private static final long serialVersionUID = 7347834262218872193L;

    public SMTPConnectionException() {
        super();
    }

    public SMTPConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SMTPConnectionException(String message) {
        super(message);
    }

    public SMTPConnectionException(Throwable cause) {
        super(cause);
    }

}
