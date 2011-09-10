package me.normanmaurer.niosmtp;

import java.io.IOException;

/**
 * Base Exception class for the SMTP Client
 * 
 * @author Norman Maurer
 *
 */
public class SMTPException extends IOException{

    /**
     * 
     */
    private static final long serialVersionUID = 3511332980540308682L;

    public SMTPException() {
        super();
    }

    public SMTPException(String message, Throwable cause) {
        super(message, cause);
    }

    public SMTPException(String message) {
        super(message);
    }

    public SMTPException(Throwable cause) {
        super(cause);
    }

}
