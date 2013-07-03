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


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLEngine;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
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

import org.slf4j.Logger;


/**
 * {@link SMTPClientSession} implementation which uses <code>NETTY</code> under the hood.
 * 
 * @author Norman Maurer
 *
 */
class NettySMTPClientSession extends AbstractSMTPClientSession {


    private final static byte CR = '\r';
    private final static byte LF = '\n';
    private final static byte DOT = '.';
    private final static byte[] DOT_CRLF = {DOT, CR, LF};
    private final static byte[] CRLF_DOT_CRLF = {CR, LF, DOT, CR, LF};
    private final static byte[] LF_DOT_CRLF = {LF, DOT, CR, LF};
    
    private final Channel channel;
    private final SSLEngine engine;
    private final SMTPClientFutureImpl<FutureResult<FutureResult.Void>> closeFuture = new SMTPClientFutureImpl<FutureResult<FutureResult.Void>>();
    private final AtomicInteger futureCount = new AtomicInteger(0);
    private static final SMTPException STARTTLS_EXCEPTION = new SMTPException("SMTPClientSession already ecrypted!");
    
    public NettySMTPClientSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode,  SSLEngine engine) {
        super(logger, config, mode, (InetSocketAddress) channel.localAddress(), (InetSocketAddress) channel.remoteAddress());      
        this.channel = channel;
        channel.pipeline().addBefore(NettyConstants.IDLE_HANDLER_KEY, "callback", new CloseHandler(closeFuture, logger));

        this.engine = engine;

    }
    

    protected void addFutureHandler(final SMTPClientFutureImpl<FutureResult<SMTPResponse>> future) {
        FutureHandler<SMTPResponse, SMTPResponse> handler = new FutureHandler<SMTPResponse, SMTPResponse>(future) {

            @Override
            public void messageReceived(ChannelHandlerContext ctx, SMTPResponse message) throws Exception {
                ctx.pipeline().remove(this);
                future.setResult(new FutureResultImpl<SMTPResponse>(message));
            }

            
        };
        addHandler(handler);

    }
    
    protected void addCollectionFutureHandler(final SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>> future, final int responsesCount) {
        FutureHandler<Collection<SMTPResponse>, SMTPResponse> handler = new FutureHandler<Collection<SMTPResponse>, SMTPResponse>(future) {
            final Collection<SMTPResponse> responses = new ArrayList<SMTPResponse>();
            @Override
            public void messageReceived(ChannelHandlerContext ctx, SMTPResponse message) throws Exception {
                responses.add(message);
                if (responses.size() == responsesCount) {
                    ctx.pipeline().remove(this);
                    future.setResult(new FutureResultImpl<Collection<SMTPResponse>>(responses));
                }
            }

        };
        addHandler(handler);

    }

    private void addHandler(ChannelHandler handler) {
        ChannelPipeline cp = channel.pipeline();
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
        return Integer.toString(channel.id());
    }

    @SuppressWarnings("unchecked")
    @Override
    public SMTPClientFuture<FutureResult<FutureResult.Void>> startTLS() {
        if (!isEncrypted()) {
            final SMTPClientFutureImpl<FutureResult<FutureResult.Void>> future = new SMTPClientFutureImpl<FutureResult<FutureResult.Void>>(false);

            SslHandler sslHandler =  new SslHandler(engine, false);
            channel.pipeline().addFirst(NettyConstants.SSL_HANDLER_KEY, sslHandler);
            sslHandler.handshakeFuture().addListener(new GenericFutureListener<Future<Channel>>() {
                
                @Override
                public void operationComplete(Future<Channel> cfuture) throws Exception {
                    if (cfuture.isSuccess()) {
                        future.setResult(FutureResult.createVoid());
                    } else {
                        future.setResult(FutureResult.create(cfuture.cause()));
                    }
                }
            });
            
            return future;
        } else {
            return new ReadySMTPClientFuture<FutureResult<FutureResult.Void>>(this, FutureResult.create(STARTTLS_EXCEPTION));
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
            
            if (extensions.contains(SMTPClientConstants._8BITMIME_EXTENSION)) {
                data = ((SMTPByteArrayMessage)msg).get8BitAsByteArray();
            } else {
                data = ((SMTPByteArrayMessage)msg).get7BitAsByteArray();
            }
            channel.write(createDataTerminatingChannelBuffer(data));
        } else {
            InputStream msgIn;
            try {

                if (extensions.contains(SMTPClientConstants._8BITMIME_EXTENSION)) {
                    msgIn = msg.get8Bit();
                } else {
                    msgIn = msg.get7bit();
                }
            
            } catch (IOException e) {
                msgIn = IOExceptionInputStream.INSTANCE;
            }
                   
            channel.write(new ChunkedStream(new DataTerminatingInputStream(msgIn)))
                    .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }
    @Override
    public SMTPClientFuture<FutureResult<FutureResult.Void>> close() {
        channel.write(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        return closeFuture;
    }


    @Override
    public boolean isEncrypted() {
        return channel.pipeline().get(SslHandler.class) != null;
    }



    @Override
    public boolean isClosed() {
        return !channel.isActive();
    }

    
    /**
     * Create a {@link ByteBuf} which is terminated with a CRLF.CRLF sequence
     *
     */
    private static ByteBuf createDataTerminatingChannelBuffer(byte[] data) {
        int length = data.length;
        if (length < 1) {
            return Unpooled.wrappedBuffer(CRLF_DOT_CRLF);
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
            return Unpooled.wrappedBuffer(data, terminating);
        }
        
      
    }
    
    private static final class CloseHandler extends ChannelInboundHandlerAdapter {
        private final SMTPClientFutureImpl<FutureResult<FutureResult.Void>> closeFuture;
        private final Logger log;


        public CloseHandler(SMTPClientFutureImpl<FutureResult<FutureResult.Void>> closeFuture, Logger log) {
            this.closeFuture = closeFuture;
            this.log = log;
        }
     
        
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            closeFuture.setResult(FutureResult.createVoid());
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (log.isDebugEnabled()) {
                log.debug("Exception during processing", cause);
            }
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
        channel.write(request).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        return future;
    }

    @Override
    public SMTPClientFuture<FutureResult<FutureResult.Void>> getCloseFuture() {
        return closeFuture;
    }
    
    
    protected abstract class FutureHandler<E,R> extends SimpleChannelInboundHandler<R> {

        protected final SMTPClientFutureImpl<FutureResult<E>> future;

        protected FutureHandler(SMTPClientFutureImpl<FutureResult<E>> future) {
            this.future = future;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.pipeline().remove(this);
            future.setResult(FutureResult.create(cause));
        }

    }
}
