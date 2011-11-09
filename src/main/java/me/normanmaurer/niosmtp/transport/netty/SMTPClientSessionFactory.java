package me.normanmaurer.niosmtp.transport.netty;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;

import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPDeliveryMode;

public interface SMTPClientSessionFactory {

    SMTPClientSession newSession(Channel channel, Logger logger, SMTPClientConfig config, SMTPDeliveryMode mode, SSLEngine engine);
}
