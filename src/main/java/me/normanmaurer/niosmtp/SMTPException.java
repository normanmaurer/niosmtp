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

import java.io.IOException;

/**
 * Base Exception class for the SMTP Client
 * 
 * @author Norman Maurer
 *
 */
public class SMTPException extends IOException{

    /**
     * 
     */
    private static final long serialVersionUID = 3511332980540308682L;

    public SMTPException() {
        super();
    }

    public SMTPException(String message, Throwable cause) {
        super(message, cause);
    }

    public SMTPException(String message) {
        super(message);
    }

    public SMTPException(Throwable cause) {
        super(cause);
    }

}
