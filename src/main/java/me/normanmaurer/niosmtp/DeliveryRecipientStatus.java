package me.normanmaurer.niosmtp;

/**
 * Status for the delivery of an email to a recipient
 * 
 * @author Norman Maurer
 *
 */
public interface DeliveryRecipientStatus {

    /**
     * Return the {@link SMTPResponse} which was returned for the recipient. 
     * 
     * @return response
     */
    public SMTPResponse getResponse();
    
    /**
     * Return true if the delivery for the recipient was successful. This is the case if
     * {@link #getReturnCode()} >= 200 && <=300
     * 
     * @return success
     */
    public boolean isSuccessful();
    
    /**
     * Return the email-address of the recipient
     * 
     * @return address
     */
    public String getAddress();
}
