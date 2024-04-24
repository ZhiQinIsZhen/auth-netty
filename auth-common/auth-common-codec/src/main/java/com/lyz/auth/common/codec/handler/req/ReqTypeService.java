package com.lyz.auth.common.codec.handler.req;

import io.netty.channel.Channel;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 14:40
 */
public interface ReqTypeService {

    /**
     * 处理请求
     *
     * @param channel netty channel
     * @param client is client
     */
    void process(Channel channel, boolean client);
}
