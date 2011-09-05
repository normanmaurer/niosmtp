package me.normanmaurer.niosmtp;

/**
 * The different SMTP Commands
 * 
 * @author Norman Maurer
 *
 */
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
    RCPT, 
    MESSAGE
}
