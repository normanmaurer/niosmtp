package me.normanmaurer.niosmtp;

/**
 * The SMTP Response which is send back from the SMTP Server to the client
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPResponse {
    
    /**
     * Return the return code
     * 
     * @return code
     */
    public int getCode();

    /**
     * Return the last line of the response which may be null
     * 
     * @return line
     */
    public String getLastLine();
}
