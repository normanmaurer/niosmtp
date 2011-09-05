package me.normanmaurer.niosmtp;

/**
 * Status for the delivery of an email to a recipient
 * 
 * @author Norman Maurer
 *
 */
public interface DeliveryRecipientStatus {

    /**
     * Return the return code which was returned for the recipient. 
     * 
     * @return code
     */
    public int getReturnCode();
    
    /**
     * Return the response String which was returned for the recipient. May be null
     * 
     * @return response
     */
    public String getResponse();
    
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
