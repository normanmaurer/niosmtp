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

import javax.net.ssl.SSLContext;

import me.normanmaurer.niosmtp.SMTPClient;
import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPUnsupportedExtensionException;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientConstants;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.impl.internal.SMTPClientPipelineFactory;
import me.normanmaurer.niosmtp.impl.internal.SecureSMTPClientPipelineFactory;

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

    private final NioClientSocketChannelFactory socketFactory = new NioClientSocketChannelFactory(createBossExecutor(), createWorkerExecutor());
    private final Timer timer = new HashedWheelTimer();
    private SSLContext context;
    private DeliveryMode mode;

    private UnpooledSMTPClient(DeliveryMode mode, SSLContext context) {
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
    

    /**
     * Create a {@link UnpooledSMTPClient} instance which use plain SMTP 
     * 
     * @return plainClient
     */
    public static UnpooledSMTPClient createPlain() {
        return new UnpooledSMTPClient(DeliveryMode.PLAIN, null);
    }
    
    /**
     * Return a {@link UnpooledSMTPClient} which use SMTPS (encrypted)
     * 
     * @param context
     * @return smtpsClient
     */
    public static UnpooledSMTPClient createSMTPS(SSLContext context) {
        return new UnpooledSMTPClient(DeliveryMode.SMTPS, context);
    }
    
    
    /**
     * Create {@link UnpooledSMTPClient} which uses plain SMTP but switch to encryption later via the STARTTLS extension
     * 
     * @param context
     * @param failOnNoSupport if true the client will throw an {@link SMTPUnsupportedExtensionException} if STARTTLS is not supported.
     *                        If false it will just continue in plain mode (no encryption)
     * @return starttlsClient
     */
    public static UnpooledSMTPClient createStartTLS(SSLContext context, boolean failOnNoSupport) {
        DeliveryMode mode;
        if (failOnNoSupport) {
            mode = DeliveryMode.STARTTLS_DEPEND;
        } else {
            mode = DeliveryMode.STARTTLS_TRY;
        }
        return new UnpooledSMTPClient(mode, context);
    }
    /*
     * (non-Javadoc)
     * @see me.normanmaurer.niosmtp.SMTPClient#deliver(java.net.InetSocketAddress, java.lang.String, java.util.Collection, java.io.InputStream, me.normanmaurer.niosmtp.SMTPClientConfig)
     */
    public SMTPClientFuture deliver(InetSocketAddress host, final String mailFrom, final Collection<String> recipients, final InputStream msg, final SMTPClientConfig config) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient must be given");
        }
        
        LinkedList<String> rcpts = new LinkedList<String>(recipients);
        

        final SMTPClientFutureImpl future = new SMTPClientFutureImpl();
        ClientBootstrap bootstrap = new ClientBootstrap(socketFactory);
        bootstrap.setOption("connectTimeoutMillis", config.getConnectionTimeout() * 1000);
        ChannelPipelineFactory cp;
        switch (mode) {
        case PLAIN:
            cp = new SMTPClientPipelineFactory(future, mailFrom, rcpts, msg, config, timer);
            break;
        case SMTPS:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_TRY:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_DEPEND:
            cp = new SecureSMTPClientPipelineFactory(future, mailFrom, rcpts, msg, config, timer, mode, context);
            break;
        default:
            throw new IllegalArgumentException("Unknown DeliveryMode " + mode);
        }

        bootstrap.setPipelineFactory(cp);
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

    @Override
    public DeliveryMode getDeliveryMode() {
        return mode;
    }
}
