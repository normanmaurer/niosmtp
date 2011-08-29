package me.normanmaurer.niosmtp;

import java.util.Collection;

public interface SMTPResponse {
    public int getCode();

    public Collection<String> getLines();
}
