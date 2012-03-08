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
package me.normanmaurer.niosmtp.delivery.chain;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.FutureResult;


/**
 * {@link me.normanmaurer.niosmtp.delivery.chain.ChainedSMTPClientFutureListener} implementation which will run after a
 * QUIT request.
 * <p/>
 * It will simply close the session, regardless of the server response.
 *
 *
 * @author Raman Gupta
 *
 */
public class QuitResponseListener implements SMTPClientFutureListener<FutureResult<SMTPResponse>> {

    /**
     * Get instance of this {@link me.normanmaurer.niosmtp.delivery.chain.QuitResponseListener} implementation
     */
    public final static QuitResponseListener INSTANCE = new QuitResponseListener();

    private QuitResponseListener() {

    }

    @Override
    public void operationComplete(SMTPClientFuture<FutureResult<SMTPResponse>> smtpResponseSMTPClientFuture) {
        // close the session no matter what the server returned
        smtpResponseSMTPClientFuture.getSession().close();
    }

}
