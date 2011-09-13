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
package me.normanmaurer.niosmtp.impl;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.normanmaurer.niosmtp.SMTPClient;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPState;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientConstants;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientHandler;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientPipelineFactory;
import me.normanmaurer.niosmtp.impl.internal.SMTPStateMachine;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

/**
 * {@link SMTPClient} implementation which will create a new Connection for
 * every
 * {@link #deliver(InetSocketAddress, String, List, InputStream, SMTPClientConfig)}
 * call.
 * 
 * So no pooling is active
 * 
 * @author Norman Maurer
 * 
 */
public class UnpooledSMTPClient implements SMTPClient, SMTPClientConstants {

    protected final NioClientSocketChannelFactory socketFactory = new NioClientSocketChannelFactory(createBossExecutor(), createWorkerExecutor());
    private final Timer timer = new HashedWheelTimer();

    protected ChannelPipelineFactory createChannelPipelineFactory() {
        return new SMTPClientPipelineFactory();
    }
    
    /**
     * Create the {@link ExecutorService} which is used for the BOSS Threads. 
     * 
     * @return bossExecutor
     */
    protected ExecutorService createBossExecutor() {
        return Executors.newCachedThreadPool();
    }
    
    /**
     * Create the {@link ExecutorService} which is used for the WORKER Threads
     * 
     * @return workerExecutor
     */
    protected ExecutorService createWorkerExecutor() {
        return Executors.newCachedThreadPool();
    }
    
    
    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClient#deliver(java.net.InetSocketAddress, java.lang.String, java.util.List, java.io.InputStream, me.normanmaurer.niosmtp.SMTPClientConfig)
     */
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final List<String> recipients, final InputStream msg, final SMTPClientConfig config) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient must be given");
        }
        
        final SMTPClientFutureImpl future = new SMTPClientFutureImpl();
        ClientBootstrap bootstrap = new ClientBootstrap(socketFactory);
        bootstrap.setOption("connectTimeoutMillis", config.getConnectionTimeout() * 1000);
        bootstrap.setPipelineFactory(createChannelPipelineFactory());
        InetSocketAddress local = config.getLocalAddress();
        bootstrap.connect(host, local).addListener(new ChannelFutureListener() {
            
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                ChannelHandlerContext context = cf.getChannel().getPipeline().getContext(SMTPClientHandler.class);
                Map<String, Object> attrs = new HashMap<String, Object>();
                attrs.put(FUTURE_KEY, future);
                attrs.put(MAIL_FROM_KEY, mailFrom);
                attrs.put(RECIPIENTS_KEY, new LinkedList<String>(recipients));
                attrs.put(MSG_KEY, msg);
                attrs.put(SMTP_CONFIG_KEY, config);
                
                SMTPStateMachine stateMatchine = new SMTPStateMachine();
                if (config.usePipelining()) {
                    stateMatchine.nextState(SMTPState.EHLO);
                } else {
                    stateMatchine.nextState(SMTPState.HELO);
                }
                attrs.put(SMTP_STATE_KEY, stateMatchine);
                context.setAttachment(attrs);

                // Add the idle timeout handler
                cf.getChannel().getPipeline().addFirst("idleHandler", new IdleStateHandler(timer, 0, 0, config.getResponseTimeout()));

                // Set the channel so we can close it for cancel later
                future.setChannel(cf.getChannel());
            }
        });
        return future;
    }

    /**
     * Call this method to destroy the {@link SMTPClient} and release all resources
     */
    public void destroy() {
        socketFactory.releaseExternalResources();
        timer.stop();
    }
}
