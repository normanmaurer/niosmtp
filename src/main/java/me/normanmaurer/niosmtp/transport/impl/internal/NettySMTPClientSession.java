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


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.MessageInput;
import me.normanmaurer.niosmtp.SMTPClientConstants;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.transport.DeliveryMode;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;


/**
 * {@link SMTPClientSession} implementation which uses <code>NETTY</code> under the hood.
 * 
 * @author Norman Maurer
 *
 */
public class NettySMTPClientSession implements SMTPClientSession, SMTPClientConstants, NettyConstants{

    private int callbackCount = 0;
    private Channel channel;
    private SSLEngine engine;
    private Set<String> extensions;
    private Logger logger;
    private Map<String, Object> attrs = new HashMap<String, Object>();
    private DeliveryMode mode;

    public NettySMTPClientSession(Channel channel, Logger logger, DeliveryMode mode,  SSLEngine engine) {
        this.logger = logger;
        this.channel = channel;
        this.engine = engine;
        this.mode = mode;
    }
    
    
    @Override
    public Set<String> getSupportedExtensions() {
        return extensions;
    }

    public void setSupportedExtensions(Set<String> extensions) {
        this.extensions = extensions;
    }
    
    @Override
    public String getId() {
        return channel.getId() + "";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }



    @Override
    public void startTLS() {
        SslHandler sslHandler =  new SslHandler(engine, false);
        channel.getPipeline().addFirst(SSL_HANDLER_KEY, sslHandler);
        sslHandler.handshake();        
    }
    
    @Override
    public void send(SMTPRequest request, SMTPResponseCallback callback) {
        channel.getPipeline().addBefore(IDLE_HANDLER_KEY, "callback" + callbackCount++, new SMTPCallbackHandlerAdapter(this, callback));
        channel.write(request);
    }
    
 

    
    @Override
    public void send(MessageInput msg, SMTPResponseCallback callback) {
        ChannelPipeline cp = channel.getPipeline();
        
        channel.getPipeline().addBefore(IDLE_HANDLER_KEY, "callback" + callbackCount++, new SMTPCallbackHandlerAdapter(this,callback));
        if (cp.get(MessageInputEncoder.class) == null) {
            channel.getPipeline().addAfter(CHUNK_WRITE_HANDLER_KEY, "messageDataEncoder", new MessageInputEncoder(this));
        }
        channel.write(msg);
            
    }
    

    @Override
    public void close() {
        channel.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }


    @Override
    public boolean isEncrypted() {
        return channel.getPipeline().get(SslHandler.class) != null;
    }


    @Override
    public Map<String, Object> getAttributes() {
        return attrs;
    }


    @Override
    public DeliveryMode getDeliveryMode() {
        return mode;
    }
    
}
