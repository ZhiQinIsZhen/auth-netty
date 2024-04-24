package com.lyz.auth.common.util.constant;

/**
 * Desc:通用常量
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 15:52
 */
public interface CommonConstant {

    String SCHEME_HTTP = "http";
    String SCHEME_HTTPS = "https";

    String SCHEME_WS = "ws";
    String SCHEME_WSS = "wss";

    String HOST_LOCAL = "127.0.0.1";

    String PROJECT_AUTHOR = "LiYZ";
    String GITHUB_URL = "https://github.com/ZhiQinIsZhen/auth-netty";
    String AUTHOR_EMAIL = "liyangzhen0114@foxmail.com";
    String DEFAULT_VERSION = "1.0.0";
    String DEFAULT_TITLE = "Netty通信接口文档";
    String DEFAULT_DESC = "一个基于Netty框架搭建的通讯推送服务";

    String OS_NAME_KEY = "os.name";
    String OS_LINUX_PREFIX = "linux";

    //默认线程数
    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    String DEFAULT_BOSS_POOL_NAME = "auth-boss-pool";
    String DEFAULT_WORKER_POOL_NAME = "auth-worker-pool";
    String DEFAULT_CLIENT_POOL_NAME = "auth-client-pool";

    String SSL_ENABLED_KEY = "ssl-enabled";

    String NETTY_SERVER_PORT = "netty.server.port";
}
