package me.normanmaurer.niosmtp;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * A client which deliver email via SMTP
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClient {

    public SMTPClientFuture deliver(InetSocketAddress host, String mailFrom, List<String> recipients, InputStream msg, SMTPClientConfig config);
}
