package com.lyz.auth.server.websocket.vo;

import com.lyz.auth.common.netty.util.JsonMapperUtil;
import lombok.Getter;
import lombok.Setter;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/28 9:11
 */
@Getter
@Setter
@Message
public class WebsocketMsgVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String msg;

    @Override
    public String toString() {
        return JsonMapperUtil.toJSONString(this);
    }
}
