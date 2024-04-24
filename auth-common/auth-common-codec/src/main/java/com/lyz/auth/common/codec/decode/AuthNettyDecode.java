package com.lyz.auth.common.codec.decode;

import com.lyz.auth.common.codec.AuthNettyMsg;
import com.lyz.auth.common.codec.exception.AuthNettyCodecException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * Desc:自定义解码器
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 14:22
 */
@Slf4j
public class AuthNettyDecode extends LengthFieldBasedFrameDecoder {

    public AuthNettyDecode() {
        super(Integer.MAX_VALUE, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = null;
        AuthNettyMsg message;
        try {
            byteBuf = (ByteBuf) super.decode(ctx, in);
            if (byteBuf == null) {
                return null;
            }
            ByteBuffer buffer = byteBuf.nioBuffer();
            message = AuthNettyMsg.decode(buffer);
        } catch (Exception e) {
            log.error("authNettyDecode exception", e);
            throw new AuthNettyCodecException();
        } finally {
            if (null != byteBuf) {
                byteBuf.release();
            }
        }
        return message;
    }
}
