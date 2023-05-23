package com.lyz.auth.common.netty.util;

import com.google.common.collect.Maps;
import com.lyz.auth.common.netty.constant.ReqType;
import com.lyz.auth.common.netty.constant.Serializable;
import com.lyz.auth.common.netty.message.MsgBody;
import com.lyz.auth.common.netty.message.MsgHeader;
import com.lyz.auth.common.netty.message.NettyMessage;
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
        if (channel == null) {
            log.warn("can not send ping cause of no channel");
            return;
        }
        if (!channel.isWritable()) {
            log.warn("can not send ping cause of channel unWritable");
        }
        NettyMessage message = new NettyMessage();
        MsgHeader header = new MsgHeader();
        header.setSerializable(serializable());
        header.setVersion(1);
        header.setReqType(ReqType.PING.getCode());
        message.setHeader(header);
        ChannelFuture future = channel.writeAndFlush(message);
        future.addListener(future1 -> {
            if (!future1.isSuccess()) {
                log.error("message send fail", future1.cause());
            }
        });
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
        if (channel == null) {
            log.warn("can not send pong cause of no channel");
            return;
        }
        if (!channel.isWritable()) {
            log.warn("can not send pong cause of channel unWritable");
        }
        NettyMessage message = new NettyMessage();
        MsgHeader header = new MsgHeader();
        header.setSerializable(serializable());
        header.setVersion(1);
        header.setReqType(ReqType.PONG.getCode());
        message.setHeader(header);
        MsgBody body = new MsgBody();
        body.setArgs("pong");
        message.setBody(body);
        ChannelFuture future = channel.writeAndFlush(message);
        future.addListener(future1 -> {
            if (!future1.isSuccess()) {
                log.error("message send fail", future1.cause());
            }
        });
    }

    private static byte serializable() {
        try {
            return Serializable.getByCode(Byte.valueOf(System.getProperty("auth.netty.serializable", "0")).byteValue()).getCode();
        } catch (Exception e) {
            return Serializable.JAVA.getCode();
        }
    }
}
