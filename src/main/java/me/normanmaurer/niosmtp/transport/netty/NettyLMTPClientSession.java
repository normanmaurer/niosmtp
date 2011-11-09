package me.normanmaurer.niosmtp.transport.netty;

import java.util.Collection;

import javax.net.ssl.SSLEngine;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPMessageSubmit;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.delivery.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;

public class NettyLMTPClientSession extends NettySMTPClientSession{

    public NettyLMTPClientSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine) {
        super(channel, logger, config, mode, engine);
    }

    @Override
    public SMTPClientFuture<FutureResult<Collection<SMTPResponse>>> send(SMTPMessageSubmit msg) {
        SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>> future = new SMTPClientFutureImpl<FutureResult<Collection<SMTPResponse>>>(false);
        future.setSMTPClientSession(this);

        addCollectionFutureHandler(future, msg.getRecipients());
        writeMessage(msg.getMessage());
        return future;
    }

    
}
