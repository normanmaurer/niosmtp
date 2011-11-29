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
package me.normanmaurer.niosmtp.transport.netty.internal;

import me.normanmaurer.niosmtp.SMTPIdleException;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;


/**
 * {@link IdleStateAwareChannelUpstreamHandler} implementation which will throw an {@link SMTPIdleException} if a connection
 * was idle for to long time
 * 
 * @author Norman Maurer
 *
 */
public class SMTPClientIdleHandler extends IdleStateAwareChannelUpstreamHandler{

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        if (e.getState() == IdleState.ALL_IDLE) {
            throw new SMTPIdleException("Connection was idling for " + (System.currentTimeMillis()- e.getLastActivityTimeMillis()) + " ms");
        }
        super.channelIdle(ctx, e);
    }

}
