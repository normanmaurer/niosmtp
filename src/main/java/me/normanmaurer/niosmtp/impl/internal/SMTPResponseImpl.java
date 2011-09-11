package me.normanmaurer.niosmtp.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

    @Override
    public String toString() {
        Iterator<String> it = lines.iterator();

        if (it.hasNext()) {
            StringBuilder sb = new StringBuilder();
            while(it.hasNext()) {
                String line = it.next();
                boolean hasNext = it.hasNext();
                sb.append(getCode());
                if (hasNext) {
                    sb.append("-");
                } else {
                    sb.append(" ");
                }
                sb.append(line);
                if (hasNext) {
                    sb.append("\r\n");
                }
                
            }
            return sb.toString();
        }
        return getCode() + "";
        
    }
}
