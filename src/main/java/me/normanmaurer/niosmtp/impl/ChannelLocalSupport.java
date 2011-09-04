package me.normanmaurer.niosmtp.impl;

import java.util.Map;

import org.jboss.netty.channel.ChannelLocal;

public interface ChannelLocalSupport {

    public static final String FUTURE_KEY = "FUTURE";
    public static final String NEXT_COMMAND_KEY = "NEXT_COMMAND";
    public static final ChannelLocal<Map<String, Object>> ATTRIBUTES = new ChannelLocal<Map<String, Object>>();
}
