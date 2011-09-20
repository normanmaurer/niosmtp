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
package me.normanmaurer.niosmtp.transport.impl.internal;

import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * An adapter class which acts as adapter between the {@link SimpleChannelUpstreamHandler} and the {@link SMTPResponseCallback}
 * 
 * 
 * @author Norman Maurer
 *
 */
public class SMTPCallbackHandlerAdapter extends SimpleChannelUpstreamHandler {
    
    private final SMTPResponseCallback callback;
    private final SMTPClientSession session;

    public SMTPCallbackHandlerAdapter(SMTPClientSession session, SMTPResponseCallback callback) {
        this.callback = callback;
        this.session = session;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof SMTPResponse) {
            callback.onResponse(session, (SMTPResponse) msg);
            
            // Remove this handler once we handed over the response to the callback
            ctx.getChannel().getPipeline().remove(this);
        } else {
            super.messageReceived(ctx, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // Don't trigger callback which was caused by the ChunkedWriteHandler
        //
        // See:
        //
        // https://issues.jboss.org/browse/NETTY-430
        if ((e.getCause() instanceof NullPointerException) == false) {
            callback.onException(session, e.getCause());
            // Remove this handler once we handed over the exception to the callback
            ctx.getChannel().getPipeline().remove(this);
        }


    }
}