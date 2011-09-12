package me.normanmaurer.niosmtp.impl.internal;

import me.normanmaurer.niosmtp.SMTPRequest;

class SMTPRequestImpl implements SMTPRequest{

    private String command;
    private String argument;

    public SMTPRequestImpl(String command, String argument) {
        this.command = command;
        this.argument = argument;
    }
    
    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String getArgument() {
        return argument;
    }
    
    @Override
    public String toString() {
        if (argument == null) {
            return command;
        } else {
            return command + " " + argument;
        }
    }

}
