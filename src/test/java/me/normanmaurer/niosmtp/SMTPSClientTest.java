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
package me.normanmaurer.niosmtp;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;

import me.normanmaurer.niosmtp.impl.UnpooledSMTPClient;

import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

public class SMTPSClientTest extends SMTPClientTest{

    @Override
    protected SMTPServer create(MessageHandlerFactory factory) {
        
        // Create a SMTPServer instance which server SMTPS requests
        return new SMTPServer(factory) {

            @Override
            protected ServerSocket createServerSocket() throws IOException {


                SSLServerSocket s = (SSLServerSocket) BogusSslContextFactory.getServerContext().getServerSocketFactory().createServerSocket(getPort(), getBacklog());
                s.setUseClientMode(false);
                s.setReuseAddress(true);
                return s;
            }
            
        };
    }


    @Override
    protected UnpooledSMTPClient createSMTPClient() {
        return UnpooledSMTPClient.createSMTPS(BogusSslContextFactory.getClientContext());
    }

}
