package me.normanmaurer.niosmtp;

/**
 * The different SMTP Commands
 * 
 * @author Norman Maurer
 *
 */
public enum SMTPCommand {
    EHLO,
    HELO,
    MAIL,
    FROM,
    DATA,
    AUTH,
    NOOP,
    QUIT, 
    RCPT, 
    DATA_POST
}
