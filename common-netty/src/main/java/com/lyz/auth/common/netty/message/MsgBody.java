package com.lyz.auth.common.netty.message;

import lombok.Getter;
import lombok.Setter;
import org.msgpack.annotation.Message;

/**
 * DESC:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/26 0:27
 */
@Getter
@Setter
@Message
public class MsgBody {

    private String op;

    private String args;
}
