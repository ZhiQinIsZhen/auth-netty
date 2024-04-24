package com.lyz.auth.common.codec.handler;

import com.lyz.auth.common.codec.AuthNettyMsg;
import com.lyz.auth.common.codec.constant.ReqType;
import com.lyz.auth.common.codec.handler.req.AbstractReqTypeService;
import com.lyz.auth.common.codec.handler.req.ReqTypeService;
import com.lyz.auth.common.codec.util.ChannelContext;
import com.lyz.auth.common.util.JsonMapperUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc:管道数据处理
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 14:29
 */
@Slf4j
@ChannelHandler.Sharable
public class AuthChannelHandler extends SimpleChannelInboundHandler<AuthNettyMsg> {

    private final boolean client;

    public AuthChannelHandler(boolean client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AuthNettyMsg msg) throws Exception {
        log.info("{} receive message: {}", client ? "client" : "server", JsonMapperUtil.toJSONString(msg));
        ReqType reqType = ReqType.getByCode(msg.getHeader().getReqType());
        if (reqType == null) {
            log.warn("unknown reqType: {}", msg.getHeader().getReqType());
            return;
        }
        ReqTypeService reqTypeService = AbstractReqTypeService.getReqTypeService(reqType);
        if (reqTypeService == null) {
            log.warn("unknown reqType: {}, not find its ReqTypeService", reqType);
            return;
        }
        reqTypeService.process(ctx.channel());
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
            AbstractReqTypeService.getReqTypeService(ReqType.PING).process(ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
    }
}
