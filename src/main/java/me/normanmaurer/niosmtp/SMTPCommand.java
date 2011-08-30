package me.normanmaurer.niosmtp;

public enum SMTPCommand {
    NONE,
    EHLO,
    HELO,
    MAIL,
    FROM,
    DATA,
    AUTH,
    NOOP,
    QUIT, 
    RCPT, MESSAGE
}
