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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.AttributeKey;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPResponseImpl;
import me.normanmaurer.niosmtp.core.StringUtils;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ByteToMessageDecoder} which decodes {@link SMTPResponse}'s. It also handles
 * multi-line responses.
 * 
 * 
 * @author Norman Maurer
 * 
 */
public class SMTPResponseDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final static Logger logger = LoggerFactory.getLogger(SMTPResponseDecoder.class);
    private final static AttributeKey<SMTPResponseImpl> key = new AttributeKey<SMTPResponseImpl>("response");


    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        SMTPResponseImpl response = ctx.attr(key).get();

        // The separator must be on index 3 as the return code has always 3
        // digits
        int separator = buf.getByte(3);

        if (separator == SMTPResponse.LAST_SEPERATOR) {
            // Ok we had a ' ' separator which means this was the end of the
            // SMTPResponse
            if (response == null) {
                int code = Integer.parseInt(buf.readBytes(3).toString(SMTPClientConstants.CHARSET));
                response = new SMTPResponseImpl(code);
                // skip the next space
                buf.skipBytes(1);
            } else {
                // skip the code and the next space
                buf.skipBytes(4);
            }

            if (buf.isReadable()) {
                response.addLine(buf.toString(SMTPClientConstants.CHARSET));
                buf.clear();
            }
            ctx.attr(key).remove();
            if (logger.isDebugEnabled()) {
                logger.debug("Channel " + ctx.channel().id() + " received: [" + StringUtils.toString(response) + "]");
            }
            return response;
        }
        if (separator == SMTPResponse.SEPERATOR) {
            // The '-' separator is used for multi-line responses so just
            // add it to the response
            if (response == null) {
                int code = Integer.parseInt(buf.readBytes(3).toString(SMTPClientConstants.CHARSET));
                response = new SMTPResponseImpl(code);
                ctx.attr(key).set(response);

                // skip the next space
                buf.skipBytes(1);

            } else {
                // skip the code and the next space
                buf.skipBytes(4);
            }
            if (buf.isReadable()) {
                response.addLine(buf.toString(SMTPClientConstants.CHARSET));
                buf.clear();
            }
        } else {
            // throw exception if the response does not have a valid format
            throw new SMTPException("Unable to parse SMTPResponse: '" + buf.toString(SMTPClientConstants.CHARSET) + "'");
        }

        return null;

    }


}
