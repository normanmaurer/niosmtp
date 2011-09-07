package me.normanmaurer.niosmtp.impl.internal;

import java.nio.charset.Charset;

import me.normanmaurer.niosmtp.SMTPResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferIndexFinder;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * {@link FrameDecoder} which decodes {@link SMTPResponse}'s. It also handles multi-line responses.
 * 
 * 
 * @author Norman Maurer
 *
 */
class SMTPResponseDecoder extends FrameDecoder {
    private final static Charset CHARSET = Charset.forName("US-ASCII");

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        buffer.markReaderIndex();
        int lineIndex = -1;
        SMTPResponseImpl response = null;
        
        // Loop over the lines
        while((lineIndex = buffer.bytesBefore(ChannelBufferIndexFinder.CRLF)) != -1) {
            ChannelBuffer line = buffer.readBytes(lineIndex);
            
            // Consume the CRLF in all cases
            while (buffer.readable()) {
                int c = buffer.getByte(buffer.readerIndex());
                if (c == '\r' || c == '\n') {
                    buffer.readByte();
                } else {
                    break;
                }
            }
            
            // The separator must be on index 3 as the return code has always 3 digits
            int separator = line.getByte(3);

            if (separator == ' ') {
                // Ok we had a ' ' separator which means this was the end of the SMTPResponse
                if (response == null) {
                    int code = Integer.parseInt(line.readBytes(3).toString(CHARSET));
                    response = new SMTPResponseImpl(code);
                }
                if (line.readable()) { 
                    response.addLine(line.toString(CHARSET));
                }
                return response;
            } else if (separator == '-') {
                // The '-' separator is used for multi-line responses so just add it to the response
                if (response == null) {
                    int code = Integer.parseInt(line.readBytes(3).toString(CHARSET));
                    response = new SMTPResponseImpl(code);
                }
                if (line.readable()) { 
                    response.addLine(line.toString(CHARSET));
                }                
            } else {
                // This should never happen but for now we just consume the line and ignore it
                return null;
            }

        }
        
        // reset the index as we not reached the end of the SMTPResponse
        buffer.resetReaderIndex();
        return response;

    }




}
