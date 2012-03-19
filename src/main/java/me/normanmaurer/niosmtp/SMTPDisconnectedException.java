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
 * {@link me.normanmaurer.niosmtp.SMTPException} which will get thrown if the connection closes unexpectedly.
 * This most times means that the SMTP Server crashed or that the network connection was interrupted.
 *
 * @author Raman Gupta
 *
 */
public class SMTPDisconnectedException extends SMTPException{

    /**
     *
     */
    private static final long serialVersionUID = 2383943619694157245L;

    public SMTPDisconnectedException() {
        super();
    }

    public SMTPDisconnectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SMTPDisconnectedException(String message) {
        super(message);
    }

    public SMTPDisconnectedException(Throwable cause) {
        super(cause);
    }

}
