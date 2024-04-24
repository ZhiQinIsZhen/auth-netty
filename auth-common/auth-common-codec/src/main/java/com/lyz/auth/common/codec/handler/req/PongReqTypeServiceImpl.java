package com.lyz.auth.common.codec.handler.req;

import com.lyz.auth.common.codec.AuthNettyMsg;
import com.lyz.auth.common.codec.constant.ReqType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc:ping
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 15:07
 */
@Slf4j
public class PongReqTypeServiceImpl extends AbstractReqTypeService {

    @Override
    protected ReqType getReqType() {
        return ReqType.PONG;
    }

    @Override
    public void process(Channel channel, boolean client) {
        if (!client) {
            return;
        }
        if (channel == null) {
            log.warn("can not send pong cause of no channel");
            return;
        }
        if (!channel.isWritable()) {
            log.warn("can not send pong cause of channel unWritable");
        }
        AuthNettyMsg message = new AuthNettyMsg();
        AuthNettyMsg.MsgHeader header = new AuthNettyMsg.MsgHeader();
        header.setSerializable(this.getSerializable().getCode());
        header.setVersion(1);
        header.setReqType(ReqType.PONG.getCode());
        message.setHeader(header);
        AuthNettyMsg.MsgBody body = new AuthNettyMsg.MsgBody();
        body.setArgs("pong");
        message.setBody(body);
        ChannelFuture future = channel.writeAndFlush(message);
        future.addListener(future1 -> {
            if (!future1.isSuccess()) {
                log.error("message send fail", future1.cause());
            }
        });
    }
}
