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

    /**
     * Deliver an email to the given {@link List} of recipients. The {@link InputStream} must be a valid email (valid encoding).
     * 
     * The delivery is done in an non-blocking fashion, so this method will return as fast as possible and then allow to get the result
     * via the {@link SMTPClientFuture}.
     * 
     * @param host
     * @param mailFrom
     * @param recipients
     * @param msg
     * @param config
     * @return future
     */
    public SMTPClientFuture deliver(InetSocketAddress host, String mailFrom, List<String> recipients, InputStream msg, SMTPClientConfig config);
}
