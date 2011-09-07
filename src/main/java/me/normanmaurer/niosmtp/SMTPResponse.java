package me.normanmaurer.niosmtp;

import java.util.List;

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
     * Return all lines of the response as an unmodifiable {@link List}. This may be null
     * 
     * @return lines
     */
    public List<String> getLines();
}
