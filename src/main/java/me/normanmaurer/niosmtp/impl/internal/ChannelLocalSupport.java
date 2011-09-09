package me.normanmaurer.niosmtp.impl.internal;

import java.util.Map;

import org.jboss.netty.channel.ChannelLocal;

public interface ChannelLocalSupport {

    public static final String FUTURE_KEY = "FUTURE";
    public static final String NEXT_COMMAND_KEY = "NEXT_COMMAND";
    public static final String CURRENT_COMMAND_KEY = "CURRENT_COMMAND";
    public static final String MAIL_FROM_KEY = "MAIL_FROM";
    public static final String RECIPIENTS_KEY = "RECIPIENTS";
    public static final String SMTP_CONFIG_KEY = "SMTP_CONFIG";
    public static final String MSG_KEY = "MSG";
    public static final String RECIPIENT_STATUS_LIST_KEY = "RECIPIENT_STATUS_LIST";

    public static final ChannelLocal<Map<String, Object>> ATTRIBUTES = new ChannelLocal<Map<String, Object>>();
}
