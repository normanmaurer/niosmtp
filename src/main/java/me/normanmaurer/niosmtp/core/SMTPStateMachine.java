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
package me.normanmaurer.niosmtp.core;

import me.normanmaurer.niosmtp.SMTPState;

/**
 * Helper class which keeps state of the current SMTP-Transaction
 * 
 * @author Norman Maurer
 *
 */
public class SMTPStateMachine {

    private SMTPState next;
    private SMTPState current;

    public void nextState(SMTPState next) {
        this.current = this.next;
        this.next = next;
    }
    
    /**
     * Return the last {@link SMTPState} of the SMTP-Transaction. This may return <code>null</code>.
     * 
     * @return currentState
     */
    public SMTPState getLastState() {
        return current;
    }
    
    /**
     * Return the next {@link SMTPState} of the SMTP-Transaction
     * 
     * @return next
     */
    public SMTPState getNextState() {
        return next;
    }
    
    
}
