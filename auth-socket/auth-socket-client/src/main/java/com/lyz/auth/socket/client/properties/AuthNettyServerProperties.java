package com.lyz.auth.socket.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/25 10:50
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.netty.server")
public class AuthNettyServerProperties {

    /**
     * 连接url
     */
    private String url;

    /**
     * 重连间隔时间
     */
    private Integer delay = 15;

    /**
     * 优雅停机等待时间,最大不超过300s
     */
    private Integer graceful = 15;
}
