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

import me.normanmaurer.niosmtp.transport.netty.internal.SecureSMTPClientPipelineFactory.SslHandshakeHandler;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;


/**
 * Constant fields which are used within the netty implementation to store state informations
 * 
 * @author Norman Maurer
 *
 */
interface NettyConstants {

    /**
     * The key to used when adding the Framer to the {@link ChannelPipeline}
     */
    public static final String FRAMER_KEY = "framer";
    
    /**
     * The key to use when adding the {@link IdleStateHandler} to the {@link ChannelPipeline}
     */
    public static final String IDLE_HANDLER_KEY = "idleHandler";
    
    /**
     * The key to use when adding the {@link ConnectHandler} to the {@link ChannelPipeline}
     */
    public static final String CONNECT_HANDLER_KEY = "connectHandler";
    
    /**
     * The key to use when adding the {@link SMTPResponseDecoder} to the {@link ChannelPipeline}
     */
    public static final String SMTP_RESPONSE_DECODER_KEY = "smtpResponseDecoder";
    
    /**
     * The key to use when adding the {@link SMTPRequestEncoder} to the {@link ChannelPipeline}
     */
    public static final String SMTP_REQUEST_ENCODER_KEY ="smtpRequestEncoder";
    
    /**
     * The key to use when adding the {@link ChunkedWriteHandler} to the {@link ChannelPipeline}
     */
    public static final String CHUNK_WRITE_HANDLER_KEY ="chunkWriteHandler";
  
    /**
     * The key to use when adding the {@link MessageInputEncoder} to the {@link ChannelPipeline}
     */
    public static final String MESSAGE_INPUT_ENCODER_KEY ="messageInputEncoder";
    
    /**
     * The key to use when adding the {@link SslHandler} to the pipeline
     */
    public static final String SSL_HANDLER_KEY = "sslHandler";
    
    /**
     * The key to use when adding the {@link SslHandshakeHandler} to the {@link ChannelPipeline}
     */
    public static final String SSL_HANDSHAKE_HANDLER_KEY = "sslHandshakeHandler";
    
    /**
     * The key to use when adding the {@link SMTPClientIdleHandler} to the pipeline
     * 
     */
    public static final String SMTP_IDLE_HANDLER_KEY = "smtpIdleHandler";
}
