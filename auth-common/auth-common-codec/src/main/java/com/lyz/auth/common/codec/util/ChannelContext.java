package com.lyz.auth.common.codec.util;

import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESC:netty channel context
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/25 22:34
 */
@Slf4j
@UtilityClass
public class ChannelContext {

    private static final Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * add channel
     *
     * @param channel channel
     * @return channel
     */
    public static Channel computeIfAbsent(Channel channel) {
        log.info("new channel connect: {}", channel.id().asLongText());
        return CHANNEL_MAP.computeIfAbsent(channel.id().asLongText(), (v) -> channel);
    }

    /**
     * get channel by channelId
     *
     * @param channelId channelId
     * @return channel
     */
    public static Channel get(String channelId) {
        return CHANNEL_MAP.get(channelId);
    }

    /**
     * remove channel
     *
     * @param channelId channelId
     */
    public static void remove(String channelId) {
        log.info("a channel disconnect: {}", channelId);
        CHANNEL_MAP.remove(channelId);
    }

    /**
     * channel count
     *
     * @return count
     */
    public static int count() {
        return CHANNEL_MAP.size();
    }
}
