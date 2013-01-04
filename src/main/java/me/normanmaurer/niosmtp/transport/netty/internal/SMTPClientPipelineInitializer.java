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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.netty.NettyConstants;
import me.normanmaurer.niosmtp.transport.netty.SMTPClientSessionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ChannelInitializer} which is used for the SMTP Client
 * 
 * @author Norman Maurer
 * 
 *
 */
@Sharable
public class SMTPClientPipelineInitializer extends ChannelInitializer<SocketChannel> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(SMTPClientPipelineInitializer.class);
    private final static SMTPRequestEncoder SMTP_REQUEST_ENCODER = new SMTPRequestEncoder();
    private final static SMTPPipeliningRequestEncoder SMTP_PIPELINING_REQUEST_ENCODER = new SMTPPipeliningRequestEncoder();
    private final static SMTPClientIdleHandler SMTP_CLIENT_IDLE_HANDLER = new SMTPClientIdleHandler();
    protected final SMTPClientFutureImpl<FutureResult<SMTPResponse>> future;
    protected final SMTPClientConfig config;
    protected final SMTPClientSessionFactory factory;
    
    public SMTPClientPipelineInitializer(SMTPClientFutureImpl<FutureResult<SMTPResponse>> future, SMTPClientConfig config, SMTPClientSessionFactory factory) {
        this.config = config;
        this.future = future;
        this.factory = factory;
    }
    
    protected SMTPConnectHandler createConnectHandler() {
        return new SMTPConnectHandler(future, LOGGER, config, SMTPDeliveryMode.PLAIN, null, factory);
    }


    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(NettyConstants.SMTP_IDLE_HANDLER_KEY, SMTP_CLIENT_IDLE_HANDLER);
        pipeline.addLast(NettyConstants.FRAMER_KEY, new DelimiterBasedFrameDecoder(8192, true, Delimiters.lineDelimiter()));

        pipeline.addLast(NettyConstants.SMTP_RESPONSE_DECODER_KEY, new SMTPResponseDecoder());
        pipeline.addLast(NettyConstants.SMTP_REQUEST_ENCODER_KEY, SMTP_REQUEST_ENCODER);
        pipeline.addLast(NettyConstants.SMTP_PIPELINING_REQUEST_ENCODER_KEY, SMTP_PIPELINING_REQUEST_ENCODER);

        pipeline.addLast(NettyConstants.CHUNK_WRITE_HANDLER_KEY, new ChunkedWriteHandler());
        pipeline.addLast(NettyConstants.DISCONNECT_HANDLER_KEY, new SMTPDisconnectHandler(future));

        // Add the idle timeout handler
        pipeline.addLast(NettyConstants.IDLE_HANDLER_KEY, new IdleStateHandler(0, 0, config.getResponseTimeout()));
        pipeline.addLast(NettyConstants.CONNECT_HANDLER_KEY, createConnectHandler());
    }

}
