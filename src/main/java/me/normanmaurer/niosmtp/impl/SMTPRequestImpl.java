package me.normanmaurer.niosmtp.impl;

import me.normanmaurer.niosmtp.SMTPRequest;

public class SMTPRequestImpl implements SMTPRequest{

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

}
