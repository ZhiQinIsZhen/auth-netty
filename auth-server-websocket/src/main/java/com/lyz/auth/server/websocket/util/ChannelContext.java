package com.lyz.auth.server.websocket.util;

import com.google.common.collect.Maps;
import com.lyz.auth.common.netty.util.RandomUtil;
import com.lyz.auth.server.websocket.vo.WebsocketMsgVO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * DESC:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/25 22:34
 */
@Slf4j
@UtilityClass
public class ChannelContext {

    private static final Map<String, Channel> CHANNEL_MAP = Maps.newConcurrentMap();

    /**
     * add
     *
     * @param channel
     * @return
     */
    public static Channel computeIfAbsent(Channel channel) {
        return CHANNEL_MAP.computeIfAbsent(channel.id().asLongText(), (v) -> channel);
    }

    /**
     * get
     *
     * @param channelId
     * @return
     */
    public static Channel get(String channelId) {
        return CHANNEL_MAP.get(channelId);
    }

    /**
     * remove
     *
     * @param channelId
     */
    public static void remove(String channelId) {
        CHANNEL_MAP.remove(channelId);
    }

    /**
     * count
     *
     * @return
     */
    public static int count() {
        return CHANNEL_MAP.size();
    }

    /**
     * ping
     *
     * @param channelId
     */
    public static void ping(String channelId) {
        ping(CHANNEL_MAP.get(channelId));
    }

    public static void ping(Channel channel) {
//        send(channel, "ping");
        send(channel, RandomUtil.randomEmoji(1, RandomUtil.EMOJI));
    }

    /**
     * pong
     *
     * @param channelId
     */
    public static void pong(String channelId) {
        pong(CHANNEL_MAP.get(channelId));
    }

    public static void pong(Channel channel) {
        send(channel, "pong");
    }

    public static void send(Channel channel, String msg) {
        if (channel == null) {
            log.warn("can not send {} cause of no channel", msg);
            return;
        }
        if (!channel.isWritable()) {
            log.warn("can not send {} cause of channel unWritable", msg);
        }
        WebsocketMsgVO msgVO = new WebsocketMsgVO();
        msgVO.setMsg(msg);
        ChannelFuture future = channel.writeAndFlush(msgVO);
        future.addListener(future1 -> {
            if (!future1.isSuccess()) {
                log.error("message send fail", future1.cause());
            }
        });
    }
}
