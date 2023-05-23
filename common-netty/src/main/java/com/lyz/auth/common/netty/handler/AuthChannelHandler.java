package com.lyz.auth.common.netty.handler;

import com.lyz.auth.common.netty.constant.ReqType;
import com.lyz.auth.common.netty.message.NettyMessage;
import com.lyz.auth.common.netty.util.ChannelContext;
import com.lyz.auth.common.netty.util.JsonMapperUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/23 15:33
 */
@Slf4j
@ChannelHandler.Sharable
public class AuthChannelHandler extends SimpleChannelInboundHandler<NettyMessage> {

    private final boolean client;

    public AuthChannelHandler(boolean client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage) throws Exception {
        log.info("{}收到消息: {}", client ? "客户端" : "服务端", JsonMapperUtil.toJSONString(nettyMessage));
        ReqType reqType = ReqType.getByCode(nettyMessage.getHeader().getReqType());
        if (reqType == null) {
            return;
        }
        switch (reqType) {
            case PING:
                ChannelContext.pong(channelHandlerContext.channel());
                break;
            case PONG:
                break;
            case REQ:
                break;
            case RES:
                break;
            default:
                break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelContext.computeIfAbsent(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelContext.remove(ctx.channel().id().asLongText());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        ChannelContext.remove(channelId);
        log.error("channelId : {}, exception!!!", channelId, cause);

        super.exceptionCaught(ctx, cause);
    }



    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState idleState = ((IdleStateEvent) evt).state();
            ChannelContext.ping(ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
    }
}
