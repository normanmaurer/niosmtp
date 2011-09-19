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

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientTransport;
import me.normanmaurer.niosmtp.SMTPUnsupportedExtensionException;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientPipelineFactory;
import me.normanmaurer.niosmtp.impl.internal.SecureSMTPClientPipelineFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

public class NettySMTPClientTransport implements SMTPClientTransport{

    private final NioClientSocketChannelFactory socketFactory = new NioClientSocketChannelFactory(createBossExecutor(), createWorkerExecutor());
    private SSLContext context;
    private DeliveryMode mode;
    private final Timer timer = new HashedWheelTimer();
    

    /**
     * Create a {@link NettySMTPClientTransport} instance which use plain SMTP 
     * 
     * @return plainClient
     */
    public static NettySMTPClientTransport createPlain() {
        return new NettySMTPClientTransport(DeliveryMode.PLAIN, null);
    }
    
    /**
     * Return a {@link NettySMTPClientTransport} which use SMTPS (encrypted)
     * 
     * @param context
     * @return smtpsClient
     */
    public static NettySMTPClientTransport createSMTPS(SSLContext context) {
        return new NettySMTPClientTransport(DeliveryMode.SMTPS, context);
    }
    
    
    /**
     * Create {@link NettySMTPClientTransport} which uses plain SMTP but switch to encryption later via the STARTTLS extension
     * 
     * @param context
     * @param failOnNoSupport if true the client will throw an {@link SMTPUnsupportedExtensionException} if STARTTLS is not supported.
     *                        If false it will just continue in plain mode (no encryption)
     * @return starttlsClient
     */
    public static NettySMTPClientTransport createStartTLS(SSLContext context, boolean failOnNoSupport) {
        DeliveryMode mode;
        if (failOnNoSupport) {
            mode = DeliveryMode.STARTTLS_DEPEND;
        } else {
            mode = DeliveryMode.STARTTLS_TRY;
        }
        return new NettySMTPClientTransport(mode, context);
    }
    private NettySMTPClientTransport(DeliveryMode mode, SSLContext context) {
        this.context = context;
        this.mode = mode;
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
    
    
    @Override
    public void connect(InetSocketAddress remote, SMTPClientConfig config, final SMTPResponseCallback callback) {
        ClientBootstrap bootstrap = new ClientBootstrap(socketFactory);
        bootstrap.setOption("connectTimeoutMillis", config.getConnectionTimeout() * 1000);
        ChannelPipelineFactory cp;
        switch (mode) {
        case PLAIN:
            cp = new SMTPClientPipelineFactory(callback, timer, config.getResponseTimeout());
            break;
        case SMTPS:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_TRY:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_DEPEND:
            cp = new SecureSMTPClientPipelineFactory(callback, timer, config.getResponseTimeout(),context, mode);
            break;
        default:
            throw new IllegalArgumentException("Unknown DeliveryMode " + mode);
        }

        bootstrap.setPipelineFactory(cp);
        InetSocketAddress local = config.getLocalAddress();
        bootstrap.connect(remote, local);
    }
    

    @Override
    public DeliveryMode getDeliveryMode() {
        return mode;
    }

    /**
     * Destroy this {@link SMTPClientTransport} and release all resources
     */
    public void destroy() {
        socketFactory.releaseExternalResources();
        timer.stop();
    }


}
