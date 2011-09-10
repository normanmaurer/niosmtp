package me.normanmaurer.niosmtp;

/**
 * {@link SMTPException} which will get thrown if the connection did idle for to long.
 * This most times means that the SMTP Server did not send a {@link SMTPResponse} in a timely manner
 * 
 * @author Norman Maurer
 *
 */
public class SMTPIdleException extends SMTPException{

    /**
     * 
     */
    private static final long serialVersionUID = 2383943619694157245L;

    public SMTPIdleException() {
        super();
    }

    public SMTPIdleException(String message, Throwable cause) {
        super(message, cause);
    }

    public SMTPIdleException(String message) {
        super(message);
    }

    public SMTPIdleException(Throwable cause) {
        super(cause);
    }

}
