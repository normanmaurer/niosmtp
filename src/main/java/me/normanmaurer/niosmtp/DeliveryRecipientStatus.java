package me.normanmaurer.niosmtp;

/**
 * Status for the delivery of an email to a recipient
 * 
 * @author Norman Maurer
 *
 */
public interface DeliveryRecipientStatus {

    public enum Status{
        Ok,
        PermanentError,
        TemporaryError
    }
    /**
     * Return the {@link SMTPResponse} which was returned for the recipient. 
     * 
     * @return response
     */
    public SMTPResponse getResponse();
    

    public Status getStatus();
    
    /**
     * Return the email-address of the recipient
     * 
     * @return address
     */
    public String getAddress();
}
