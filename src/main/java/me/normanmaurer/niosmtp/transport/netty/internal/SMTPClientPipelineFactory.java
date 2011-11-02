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

import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ChannelPipelineFactory} which is used for the SMTP Client
 * 
 * @author Norman Maurer
 * 
 *
 */
public class SMTPClientPipelineFactory implements ChannelPipelineFactory, NettyConstants{
    protected final static Logger LOGGER = LoggerFactory.getLogger(SMTPClientPipelineFactory.class);
    private final static SMTPResponseDecoder SMTP_RESPONSE_DECODER = new SMTPResponseDecoder();
    private final static SMTPRequestEncoder SMTP_REQUEST_ENCODER = new SMTPRequestEncoder();
    private final static SMTPPipeliningRequestEncoder SMTP_PIPELINING_REQUEST_ENCODER = new SMTPPipeliningRequestEncoder();
    private final static SMTPClientIdleHandler SMTP_CLIENT_IDLE_HANDLER = new SMTPClientIdleHandler();
    private final Timer timer;
    protected final SMTPResponseCallback callback;
    protected final SMTPClientConfig config;
    
    public SMTPClientPipelineFactory(SMTPResponseCallback callback, SMTPClientConfig config, Timer timer) {
        this.timer = timer;
        this.config = config;
        this.callback = callback;
    }
    
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast(SMTP_IDLE_HANDLER_KEY, SMTP_CLIENT_IDLE_HANDLER);
        pipeline.addLast(FRAMER_KEY, new DelimiterBasedFrameDecoder(8192, true, Delimiters.lineDelimiter()));
        pipeline.addLast(SMTP_RESPONSE_DECODER_KEY, SMTP_RESPONSE_DECODER);
        pipeline.addLast(SMTP_REQUEST_ENCODER_KEY, SMTP_REQUEST_ENCODER);
        pipeline.addLast(SMTP_PIPELINING_REQUEST_ENCODER_KEY, SMTP_PIPELINING_REQUEST_ENCODER);

        pipeline.addLast(CHUNK_WRITE_HANDLER_KEY, new ChunkedWriteHandler());
        
        // Add the idle timeout handler
        pipeline.addLast(IDLE_HANDLER_KEY, new IdleStateHandler(timer, 0, 0, config.getResponseTimeout()));
        pipeline.addLast(CONNECT_HANDLER_KEY, createConnectHandler());
        return pipeline;
    }
    
    protected ConnectHandler createConnectHandler() {
        return new ConnectHandler(callback, LOGGER, config, SMTPDeliveryMode.PLAIN, null);
    }
    


}
