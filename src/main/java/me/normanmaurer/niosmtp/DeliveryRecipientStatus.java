package me.normanmaurer.niosmtp;

/**
 * Status for the delivery of an email to a recipient
 * 
 * @author Norman Maurer
 *
 */
public interface DeliveryRecipientStatus {

    public enum Status{
        /**
         * Email was successful delivered to the recipient
         * 
         */
        Ok,
        
        /**
         * Email was permanent reject for the recipient
         */
        PermanentError,
        
        /**
         * Email was temporar reject for the recipient
         */
        TemporaryError
    }
    
    /**
     * Return the {@link SMTPResponse} which was returned for the recipient. 
     * 
     * @return response
     */
    public SMTPResponse getResponse();
    

    /**
     * Return the status
     * 
     * @return status
     */
    public Status getStatus();
    
    /**
     * Return the email-address of the recipient
     * 
     * @return address
     */
    public String getAddress();
}
