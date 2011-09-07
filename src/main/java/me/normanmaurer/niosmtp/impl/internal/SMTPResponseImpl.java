package me.normanmaurer.niosmtp.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPResponse;

class SMTPResponseImpl implements SMTPResponse{

    private int code;
    private final List<String> lines = new ArrayList<String>();

    public SMTPResponseImpl(int code) {
        this.code = code;
    }
    @Override
    public int getCode() {
        return code;
    }

    public void addLine(String line) {
        lines.add(line);
    }

    @Override
    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

}
