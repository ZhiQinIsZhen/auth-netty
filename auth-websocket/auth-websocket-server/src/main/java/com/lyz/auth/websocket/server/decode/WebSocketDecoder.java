package com.lyz.auth.websocket.server.decode;

import com.lyz.auth.websocket.server.util.ChannelContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.MessagePack;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/28 11:11
 */
@Slf4j
@ChannelHandler.Sharable
public class WebSocketDecoder extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            if (this.webSocketChannelRead(ctx, (WebSocketFrame) msg)) {
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ChannelContext.ping(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private boolean webSocketChannelRead(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if (msg instanceof PingWebSocketFrame) {
            try {
                ctx.fireChannelRead("ping");
            } finally {
                msg.release();
            }
            return true;
        }
        if (msg instanceof PongWebSocketFrame) {
            try {
                ctx.fireChannelRead("pong");
            } finally {
                msg.release();
            }
            return true;
        }
        if (msg instanceof TextWebSocketFrame) {
            final TextWebSocketFrame frame = (TextWebSocketFrame) msg;
            String text = frame.text();
            try {
                ctx.fireChannelRead(text);
            } finally {
                msg.release();
            }
            return true;
        }
        if (msg instanceof BinaryWebSocketFrame) {
            try {
                final BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
                ByteBuf content = frame.content();
                final int length = content.readableBytes();
                final byte[] array = new byte[length];
                content.getBytes(content.readerIndex(), array, 0, length);
                MessagePack messagePack = new MessagePack();
                String msgStr = messagePack.read(array, String.class);
                ctx.fireChannelRead(msgStr);
            } finally {
                msg.release();
            }
            return true;
        }
        return false;
    }
}
