package com.lyz.auth.common.codec.exception;

/**
 * Desc:编解码异常
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 14:25
 */
public class AuthNettyCodecException extends RuntimeException{
    private static final long serialVersionUID = -4491200502532480571L;

    public AuthNettyCodecException() {
        super();
    }

    public AuthNettyCodecException(String message) {
        super(message);
    }

    public AuthNettyCodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
