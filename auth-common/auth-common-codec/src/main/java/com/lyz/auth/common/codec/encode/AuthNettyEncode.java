package com.lyz.auth.common.codec.encode;

import com.lyz.auth.common.codec.AuthNettyMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;

/**
 * Desc:自定义编码器
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 11:00
 */
@ChannelHandler.Sharable
public class AuthNettyEncode extends MessageToByteEncoder<AuthNettyMsg> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AuthNettyMsg authNettyMsg, ByteBuf byteBuf) throws Exception {
        byte[] body = authNettyMsg.bodyEncode();
        ByteBuffer header = authNettyMsg.encodeHeader(body != null ? body.length : 0);
        byteBuf.writeBytes(header);
        if (body != null) {
            byteBuf.writeBytes(body);
        }
    }
}
