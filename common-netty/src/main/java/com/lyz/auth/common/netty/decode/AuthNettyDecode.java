package com.lyz.auth.common.netty.decode;

import com.lyz.auth.common.netty.message.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/23 14:51
 */
@Slf4j
public class AuthNettyDecode extends LengthFieldBasedFrameDecoder {

    public AuthNettyDecode() {
        super(Integer.MAX_VALUE, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = null;
        NettyMessage message;
        try {
            byteBuf = (ByteBuf) super.decode(ctx, in);
            if (byteBuf == null) {
                return null;
            }
            ByteBuffer buffer = byteBuf.nioBuffer();
            message = NettyMessage.decode(buffer);
        } catch (Exception e) {
            log.error("authNettyDecode exception", e);
            return null;
        } finally {
            if (null != byteBuf) {
                byteBuf.release();
            }
        }
        return message;
    }
}
