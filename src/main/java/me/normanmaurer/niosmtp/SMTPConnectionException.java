package me.normanmaurer.niosmtp;

/**
 * {@link SMTPException} which will get thrown if the connection was timed out or refused
 * 
 * @author Norman Maurer
 *
 */
public class SMTPConnectionException extends SMTPException{

    /**
     * 
     */
    private static final long serialVersionUID = 7347834262218872193L;

    public SMTPConnectionException() {
        super();
    }

    public SMTPConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SMTPConnectionException(String message) {
        super(message);
    }

    public SMTPConnectionException(Throwable cause) {
        super(cause);
    }

}
