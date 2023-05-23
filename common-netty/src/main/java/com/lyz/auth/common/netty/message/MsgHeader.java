package com.lyz.auth.common.netty.message;

import lombok.Getter;
import lombok.Setter;
import org.msgpack.annotation.Message;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/21 15:13
 */
@Getter
@Setter
@Message
public class MsgHeader {

    /**
     * 版本
     */
    private int version;

    /**
     *
     */
    private byte serializable;

    /**
     *
     */
    private byte reqType;
}
