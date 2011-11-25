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
package me.normanmaurer.niosmtp.transport.netty;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPByteArrayMessage;
import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPMessageSubmit;
import me.normanmaurer.niosmtp.SMTPPipeliningRequest;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.DataTerminatingInputStream;
import me.normanmaurer.niosmtp.core.ReadySMTPClientFuture;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.transport.AbstractSMTPClientSession;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;
import me.normanmaurer.niosmtp.transport.impl.FutureResultImpl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedStream;
import org.slf4j.Logger;


/**
 * {@link SMTPClientSession} implementation which uses <code>NETTY</code> under the hood.
 * 
 * @author Norman Maurer
 *
 */
class NettySMTPClientSession extends AbstractSMTPClientSession implements SMTPClientSession, SMTPClientConstants, NettyConstants{


    private final static byte CR = '\r';
    private final static byte LF = '\n';
    private final static byte DOT = '.';
    private final static byte[] DOT_CRLF = new byte[] {DOT, CR, LF};
    private final static byte[] CRLF_DOT_CRLF = new byte[] {CR, LF, DOT, CR, LF};
    private final static byte[] LF_DOT_CRLF = new byte[] {LF, DOT, CR, LF};
    
    private final Channel channel;
    private final SSLEngine engine;
    private final SMTPClientFutureImpl<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> closeFuture = new SMTPClientFutureImpl<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>>();
    private final AtomicInteger futureCount = new AtomicInteger(0);
    private static final SMTPException STARTTLS_EXCEPTION = new SMTPException("SMTPClientSession already ecrypted!");
    
    public NettySMTPClientSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode,  SSLEngine engine) {
        super(logger, config, mode, (InetSocketAddress) channel.getLocalAddress(), (InetSocketAddress) channel.getRemoteAddress());      
        this.channel = channel;
        channel.getPipeline().addBefore(IDLE_HANDLER_KEY, "callback", new CloseHandler(closeFuture, logger));

        this.engine = engine;

    }
    

    protected void addFutureHandler(final SMTPClientFutureImpl<FutureResult<SMTPResponse>> future) {
        SimpleChannelUpstreamHandler handler = new FutureHandler<SMTPResponse>(future) {

            @Override
            public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                if (e.getMessage() instanceof SMTPResponse) {
                    ctx.getPipeline().remove(this);
                    future.setResult(new FutureResultImpl<SMTPResponse>((SMTPResponse)e.getMessage()));
                } else {
                    super.messageReceived(ctx, e);
                }
            }

            
        };
        addHandler(handler);

    }
    
    protected void addCollectionFutureHandler(final SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>> future, final int responsesCount) {
        FutureHandler<Collection<SMTPResponse>> handler = new FutureHandler<Collection<SMTPResponse>>(future) {
            final Collection<SMTPResponse> responses = new ArrayList<SMTPResponse>();
            @Override
            public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                if (e.getMessage() instanceof SMTPResponse) {
                    responses.add((SMTPResponse) e.getMessage());
                    if (responses.size() == responsesCount) {
                        ctx.getPipeline().remove(this);
                        future.setResult(new FutureResultImpl<Collection<SMTPResponse>>(responses));
                    }
                } else {
                    super.messageReceived(ctx, e);
                }
            }

            
        };
        addHandler(handler);

    }

    private void addHandler(SimpleChannelUpstreamHandler handler) {
        ChannelPipeline cp = channel.getPipeline();
        int count = futureCount.incrementAndGet();
        String oldHandler = "futureHandler" + (count -1);
        if (count == 1 || cp.get(oldHandler) == null) {
            cp.addBefore("callback", "futureHandler" + count, handler);
        } else {
            cp.addBefore(oldHandler, "futureHandler" + count, handler);
        }
    }
    
    @Override
    public String getId() {
        return channel.getId() + "";
    }

    @SuppressWarnings("unchecked")
    @Override
    public SMTPClientFuture<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> startTLS() {
        if (!isEncrypted()) {
            final SMTPClientFutureImpl<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> future = new SMTPClientFutureImpl<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>>(false);

            SslHandler sslHandler =  new SslHandler(engine, false);
            channel.getPipeline().addFirst(SSL_HANDLER_KEY, sslHandler);
            sslHandler.handshake().addListener(new ChannelFutureListener() {
                
                @Override
                public void operationComplete(ChannelFuture cfuture) throws Exception {
                    if (cfuture.isSuccess()) {
                        future.setResult(FutureResult.createVoid());
                    } else {
                        future.setResult(FutureResult.create(cfuture.getCause()));
                    }
                }
            });
            
            return future;
        } else {
            return new ReadySMTPClientFuture<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>>(this, FutureResult.create(STARTTLS_EXCEPTION));
        }
    }
    
    @Override
    public SMTPClientFuture<FutureResult<SMTPResponse>> send(SMTPRequest request) {
        SMTPClientFutureImpl<FutureResult<SMTPResponse>> future = new SMTPClientFutureImpl<FutureResult<SMTPResponse>>(false);
        future.setSMTPClientSession(this);
        addFutureHandler(future);
        channel.write(request);
        return future;
    }
    
 

    
    @Override
    public SMTPClientFuture<FutureResult<Collection<SMTPResponse>>> send(SMTPMessageSubmit msg) {
        SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>> future = new SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>>(false);
        future.setSMTPClientSession(this);

        addCollectionFutureHandler(future,1 );
        writeMessage(msg.getMessage());
        return future;
                    
    }
    

    protected void writeMessage(SMTPMessage msg) {
        Set<String> extensions = getSupportedExtensions();

        if (msg instanceof SMTPByteArrayMessage) {
            byte[] data;
            
            if (extensions.contains(_8BITMIME_EXTENSION)) {
                data = ((SMTPByteArrayMessage)msg).get8BitAsByteArray();
            } else {
                data = ((SMTPByteArrayMessage)msg).get7BitAsByteArray();
            }
            channel.write(createDataTerminatingChannelBuffer(data));
        } else {
            InputStream msgIn;
            try {

                if (extensions.contains(_8BITMIME_EXTENSION)) {
                    msgIn = msg.get8Bit();
                } else {
                    msgIn = msg.get7bit();
                }
            
            } catch (IOException e) {
                msgIn = IOExceptionInputStream.INSTANCE;
            }
                   
            channel.write(new ChunkedStream(new DataTerminatingInputStream(msgIn)));
        }
    }
    @Override
    public SMTPClientFuture<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> close() {
        channel.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        return closeFuture;
    }


    @Override
    public boolean isEncrypted() {
        return channel.getPipeline().get(SslHandler.class) != null;
    }



    @Override
    public boolean isClosed() {
        return !channel.isConnected();
    }

    
    /**
     * Create a {@link ChannelBuffer} which is terminated with a CRLF.CRLF sequence
     * 
     * @param data
     * @return buffer
     */
    private static ChannelBuffer createDataTerminatingChannelBuffer(byte[] data) {
        int length = data.length;
        if (length < 1) {
            return ChannelBuffers.wrappedBuffer(CRLF_DOT_CRLF);
        } else {
            byte[] terminating;

            byte last = data[length -1];

            if (length == 1) {
                if (last == CR) {
                    terminating = LF_DOT_CRLF;
                } else {
                    terminating = CRLF_DOT_CRLF;
                }
            } else {
                byte prevLast = data[length - 2];
                
                if (last == LF) {
                    if (prevLast == CR) {
                        terminating = DOT_CRLF;
                    } else {
                        terminating = CRLF_DOT_CRLF;
                    }
                } else if (last == CR) {
                    terminating = LF_DOT_CRLF;
                } else {
                    terminating = CRLF_DOT_CRLF;

                }
            }
            return ChannelBuffers.wrappedBuffer(data, terminating);
        }
        
      
    }
    
    private static final class CloseHandler extends SimpleChannelUpstreamHandler {
        private final SMTPClientFutureImpl<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> closeFuture;
        private final Logger log;


        public CloseHandler(SMTPClientFutureImpl<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> closeFuture, Logger log) {
            this.closeFuture = closeFuture;
            this.log = log;
        }
     
        
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            if (log.isDebugEnabled()) {
                log.debug("Exception during processing", e.getCause());
            }
        }



        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            closeFuture.setResult(FutureResult.createVoid());
            super.channelClosed(ctx, e);
        }
    }
    
    
    private final static class IOExceptionInputStream extends InputStream {
        public final static IOExceptionInputStream INSTANCE= new IOExceptionInputStream();
        
        @Override
        public int read() throws IOException {
            throw new IOException("Unable to read content");
        }
        
    }


    @Override
    public SMTPClientFuture<FutureResult<Collection<SMTPResponse>>> send(SMTPPipeliningRequest request) {
        
        SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>> future = new SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>>(false);
        future.setSMTPClientSession(this);

        final int requests = request.getRequests().size();
        addCollectionFutureHandler(future, requests);
        channel.write(request);      
        return future;
    }

    @Override
    public SMTPClientFuture<FutureResult<me.normanmaurer.niosmtp.transport.FutureResult.Void>> getCloseFuture() {
        return closeFuture;
    }
    
    
    protected abstract class FutureHandler<E> extends SimpleChannelUpstreamHandler {

        protected SMTPClientFutureImpl<FutureResult<E>> future;


        public FutureHandler(SMTPClientFutureImpl<FutureResult<E>> future) {
            this.future = future;
        }
       

        @SuppressWarnings("unchecked")
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            ctx.getPipeline().remove(this);
            future.setResult(FutureResult.create(e.getCause()));
            
        }
        
    }
}
