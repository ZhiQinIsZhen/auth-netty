package com.lyz.auth.server.websocket.handler;

import com.lyz.auth.server.websocket.dto.WebsocketMsgDTO;
import com.lyz.auth.server.websocket.util.ChannelContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
public class AuthWebsocketChannelHandler extends SimpleChannelInboundHandler<WebsocketMsgDTO> {

    private final boolean client;

    public AuthWebsocketChannelHandler(boolean client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebsocketMsgDTO websocketMsg) throws Exception {
        String msg = websocketMsg.getMsg();
        log.info("{}收到消息: {}", client ? "客户端" : "服务端", msg);
        switch (msg) {
            case "ping":
                ChannelContext.pong(ctx.channel());
                break;
            case "pong":
                ChannelContext.send(ctx.channel(), "hello");
                break;
            default:
                ChannelContext.send(ctx.channel(), msg);
                break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        ChannelContext.computeIfAbsent(ctx.channel());
        log.warn("channelId : {}, added", channelId);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        ChannelContext.remove(channelId);
        log.warn("channelId : {}, removed", channelId);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        ChannelContext.remove(channelId);
        log.error("channelId : {}, exception!!!", channelId, cause);
        super.exceptionCaught(ctx, cause);
    }
}
