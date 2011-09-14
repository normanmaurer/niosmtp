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
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.normanmaurer.niosmtp.SMTPClient;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientConstants;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientPipelineFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

/**
 * {@link SMTPClient} implementation which will create a new Connection for every
 * {@link #deliver(InetSocketAddress, String, Collection, InputStream, SMTPClientConfig)} call.
 * 
 * So no pooling is active
 * 
 * @author Norman Maurer
 * 
 */
public class UnpooledSMTPClient implements SMTPClient, SMTPClientConstants {

    protected final NioClientSocketChannelFactory socketFactory = new NioClientSocketChannelFactory(createBossExecutor(), createWorkerExecutor());
    protected final Timer timer = new HashedWheelTimer();

    protected ChannelPipelineFactory createChannelPipelineFactory(SMTPClientFutureImpl future, String mailFrom, LinkedList<String> recipients, InputStream msg, SMTPClientConfig config) {
        return new SMTPClientPipelineFactory(future, mailFrom, recipients, msg, config, timer);
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
     * @see me.normanmaurer.niosmtp.SMTPClient#deliver(java.net.InetSocketAddress, java.lang.String, java.util.Collection, java.io.InputStream, me.normanmaurer.niosmtp.SMTPClientConfig)
     */
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final Collection<String> recipients, final InputStream msg, final SMTPClientConfig config) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient must be given");
        }
        
        final SMTPClientFutureImpl future = new SMTPClientFutureImpl();
        ClientBootstrap bootstrap = new ClientBootstrap(socketFactory);
        bootstrap.setOption("connectTimeoutMillis", config.getConnectionTimeout() * 1000);
        bootstrap.setPipelineFactory(createChannelPipelineFactory(future, mailFrom, new LinkedList<String>(recipients), msg, config));
        InetSocketAddress local = config.getLocalAddress();
        bootstrap.connect(host, local);
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
