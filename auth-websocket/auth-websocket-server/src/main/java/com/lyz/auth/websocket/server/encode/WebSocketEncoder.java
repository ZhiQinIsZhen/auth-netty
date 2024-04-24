package com.lyz.auth.websocket.server.encode;

import com.lyz.auth.websocket.server.vo.WebsocketMsgVO;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/28 11:31
 */
@Slf4j
@ChannelHandler.Sharable
public class WebSocketEncoder extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof WebsocketMsgVO) {
            WebsocketMsgVO msgVO  = (WebsocketMsgVO) msg;
            final TextWebSocketFrame frame = new TextWebSocketFrame();
            frame.content().writeBytes(msgVO.toString().getBytes());
            ctx.writeAndFlush(frame);
        }
        super.write(ctx, msg, promise);
    }
}
