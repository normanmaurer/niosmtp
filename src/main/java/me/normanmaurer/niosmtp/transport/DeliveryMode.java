package me.normanmaurer.niosmtp.transport;



public enum DeliveryMode {
    PLAIN,
    SMTPS,
    STARTTLS_TRY,
    STARTTLS_DEPEND
}
