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
package me.normanmaurer.niosmtp.impl.internal;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

/**
 * {@link ChannelPipelineFactory} which is used for the SMTP Client
 * 
 * @author Norman Maurer
 * 
 *
 */
public class SMTPClientPipelineFactory implements ChannelPipelineFactory{

    private final static DelimiterBasedFrameDecoder FRAMER = new DelimiterBasedFrameDecoder(8192, true, Delimiters.lineDelimiter());
    private final static SMTPResponseDecoder SMTP_RESPONSE_DECODER = new SMTPResponseDecoder();
    private final static SMTPRequestEncoder SMTP_REQUEST_ENCODER = new SMTPRequestEncoder();
    private final static SMTPClientHandler SMTP_CLIENT_HANDLER = new SMTPClientHandler();
    private final static SMTPClientIdleHandler SMTP_CLIENT_IDLE_HANDLER = new SMTPClientIdleHandler();
    private final static SMTPPipelinedRequestEncoder SMTP_PIPELINE_REQUEST_ENCODER = new SMTPPipelinedRequestEncoder();
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("smtpIdleHandler", SMTP_CLIENT_IDLE_HANDLER);
        pipeline.addLast("framer", FRAMER);
        pipeline.addLast("decoder", SMTP_RESPONSE_DECODER);
        pipeline.addLast("encoder", SMTP_REQUEST_ENCODER);
        pipeline.addLast("pipelinedEncoder", SMTP_PIPELINE_REQUEST_ENCODER);
        pipeline.addLast("chunk", new ChunkedWriteHandler());
        pipeline.addLast("coreHandler", SMTP_CLIENT_HANDLER);
        return pipeline;
    }

}
