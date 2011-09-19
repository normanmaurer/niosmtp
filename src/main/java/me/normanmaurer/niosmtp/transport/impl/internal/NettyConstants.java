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
package me.normanmaurer.niosmtp.transport.impl.internal;

interface NettyConstants {

    public static final String FRAMER_KEY = "framer";
    public static final String IDLE_HANDLER_KEY = "idleHandler";
    public static final String CONNECT_HANDLER_KEY = "connectHandler";
    public static final String SMTP_RESPONSE_DECODER_KEY = "smtpResponseDecoder";
    public static final String SMTP_REQUEST_ENCODER_KEY ="smtpRequestEncoder";
    public static final String CHUNK_WRITE_HANDLER_KEY ="chunkWriteHandler";
    public static final String MESSAGE_INPUT_ENCODER_KEY ="messageInputEncoder";
    public static final String SSL_HANDLER_KEY = "sslHandler";
    public static final String SSL_HANDSHAKE_HANDLER_KEY = "sslHandshakeHandler";
    public static final String SMTP_IDLE_HANDLER_KEY = "smtpIdleHandler";
}
