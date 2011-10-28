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


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPMultiResponseCallback;
import me.normanmaurer.niosmtp.SMTPByteArrayMessage;
import me.normanmaurer.niosmtp.SMTPMessage;
import me.normanmaurer.niosmtp.SMTPPipeliningRequest;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.DataTerminatingInputStream;
import me.normanmaurer.niosmtp.transport.AbstractSMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
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
    private final List<CloseListener> closeListeners = new ArrayList<CloseListener>();
    private final LinkedList<SMTPResponseCallback> callbacks = new LinkedList<SMTPResponseCallback>();
    
    

    private NettySMTPClientSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode,  SSLEngine engine) {
        super(logger, config, mode, (InetSocketAddress) channel.getLocalAddress(), (InetSocketAddress) channel.getRemoteAddress());      
        this.channel = channel;
        this.engine = engine;

    }
    
    public static NettySMTPClientSession create(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine) {
        NettySMTPClientSession session = new NettySMTPClientSession(channel, logger, config, mode, engine);
        session.channel.getPipeline().addBefore(IDLE_HANDLER_KEY, "callback", new CallbackAdapter(session));
        return session;
    }
    
    
    
    @Override
    public String getId() {
        return channel.getId() + "";
    }

    @Override
    public void startTLS() {
        if (!isEncrypted()) {
            SslHandler sslHandler =  new SslHandler(engine, false);
            channel.getPipeline().addFirst(SSL_HANDLER_KEY, sslHandler);
            sslHandler.handshake();
        }
    }
    
    @Override
    public void send(SMTPRequest request, SMTPResponseCallback callback) {
        callbacks.add(callback);
        channel.write(request);
    }
    
 

    
    @Override
    public void send(SMTPMessage msg, SMTPResponseCallback callback) {
        callbacks.add(callback);

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
    public void close() {
        channel.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }


    @Override
    public boolean isEncrypted() {
        return channel.getPipeline().get(SslHandler.class) != null;
    }


    @Override
    public void addCloseListener(CloseListener listener) {
        closeListeners.add(listener);
    }


    @Override
    public boolean isClosed() {
        return !channel.isConnected();
    }

    @Override
    public void removeCloseListener(CloseListener listener) {
        closeListeners.remove(listener);
       
    }

    @Override
    public Iterator<CloseListener> getCloseListeners() {
        return new ArrayList<CloseListener>(closeListeners).iterator();
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
    
    protected static final class CallbackAdapter extends SimpleChannelUpstreamHandler {
        private final NettySMTPClientSession session;


        public CallbackAdapter(NettySMTPClientSession session) {
            this.session = session;
        }
        
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            Object msg = e.getMessage();
            if (msg instanceof SMTPResponse) {
                SMTPResponseCallback callback = session.callbacks.peek();
                if (callback != null) {
                    callback.onResponse(session, (SMTPResponse) msg);
                    
                    boolean remove = true;
                    if (callback instanceof SMTPMultiResponseCallback && !((SMTPMultiResponseCallback) callback).isDone(session)) {
                        remove = false;
                    }
                    if (remove) {
                        session.callbacks.remove(callback);
                    }
                }
            } else {
                super.messageReceived(ctx, e);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            SMTPResponseCallback callback = session.callbacks.poll();

            if (callback != null) {
                callback.onException(session, e.getCause());
            }

        }
        
        
        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            for (CloseListener listener: session.closeListeners) {
                listener.onClose(session);
            }
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
    public void send(SMTPPipeliningRequest request, SMTPMultiResponseCallback callback) {
        callbacks.add(callback);
        channel.write(request);        
    }
}
