package com.lyz.auth.websocket.server.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/25 11:12
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.netty.server")
public class AuthNettyServerProperties {

    /**
     * 端口号
     */
    private int port = 9998;

    /**
     * 优雅停机等待时间(单位:s),最大不超过300s
     */
    private Integer graceful = 15;

    /**
     * 读空闲检测时间(单位:s)
     */
    private long readerIdleTime = 0;

    /**
     * 写空闲检测时间(单位:s)
     */
    private long writerIdleTime = 0;

    /**
     * 心跳检测间隔时间(单位:s)
     */
    private long allIdleTime = 15;

    /**
     * sll开关
     */
    private boolean sslEnabled = false;

    /**
     * sll配置文件地址
     */
    private String sslPathName;

    /**
     * 前缀path
     */
    private String websocketPath = "/ws";
}
