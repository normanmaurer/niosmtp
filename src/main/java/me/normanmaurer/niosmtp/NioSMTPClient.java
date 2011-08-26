package me.normanmaurer.niosmtp;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Future;

public interface NioSMTPClient {

    public Future<List<RecipientStatus>> deliver(String mailFrom, List<String> recipients, InputStream msg, String heloName, int timeout);
}
