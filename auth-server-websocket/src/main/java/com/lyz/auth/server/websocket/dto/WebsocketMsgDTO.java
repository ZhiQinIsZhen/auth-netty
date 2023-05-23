package com.lyz.auth.server.websocket.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/28 9:18
 */
@Getter
@Setter
public class WebsocketMsgDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String msg;
}
