package me.normanmaurer.niosmtp;

/**
 * A SMTP Request which is send to the SMTP-Server
 * 
 * @author Norman Maurer
 *
 */
public interface SMTPRequest {
    
    /**
     * Return the command 
     * 
     * @return command
     */
    public String getCommand();
    
    /**
     * Return the argument. This may be null
     * 
     * @return argument
     */
    public String getArgument();
}
