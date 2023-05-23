package com.lyz.auth.common.netty.encode;

import com.lyz.auth.common.netty.message.NettyMessage;
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
 * @date 2023/3/21 15:10
 */
@ChannelHandler.Sharable
public class AuthNettyEncode extends MessageToByteEncoder<NettyMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage nettyMessage, ByteBuf out) throws Exception {
        byte[] body = nettyMessage.bodyEncode();
        ByteBuffer header = nettyMessage.encodeHeader(body != null ? body.length : 0);
        out.writeBytes(header);
        if (body != null) {
            out.writeBytes(body);
        }
    }
}
