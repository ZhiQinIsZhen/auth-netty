package com.lyz.auth.server.websocket.encode;

import com.lyz.auth.server.websocket.vo.WebsocketMsgVO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/28 9:37
 */
@Slf4j
@ChannelHandler.Sharable
public class MsgPackEncoder extends MessageToByteEncoder<WebsocketMsgVO> {

    @Override
    protected void encode(ChannelHandlerContext ctx, WebsocketMsgVO websocketMsgVO, ByteBuf byteBuf) throws Exception {
        ctx.writeAndFlush(websocketMsgVO);
    }
}
