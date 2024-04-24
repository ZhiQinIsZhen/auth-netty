package com.lyz.auth.websocket.server.decode;

import com.lyz.auth.websocket.server.dto.WebsocketMsgDTO;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/28 9:38
 */
@Slf4j
@ChannelHandler.Sharable
public class MsgPackDecoder extends MessageToMessageDecoder<String> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String msg, List<Object> list) throws Exception {
        WebsocketMsgDTO msgDTO = new WebsocketMsgDTO();
        msgDTO.setMsg(msg);
        list.add(msgDTO);
    }
}
