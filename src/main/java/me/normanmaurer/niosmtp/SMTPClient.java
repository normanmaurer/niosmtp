package me.normanmaurer.niosmtp;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Future;

/**
 * A client which deliver email via SMTP
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPClient {

    public Future<List<RecipientStatus>> deliver(InetSocketAddress host, String mailFrom, List<String> recipients, InputStream msg, SMTPClientConfig config);
}
