package com.lyz.auth.common.netty.constant;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/21 15:05
 */
public interface AuthNettyConstant {

    int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    String EVENT_LOOP_BOSS_POOL_NAME  = "NettyServerBoss";

    String EVENT_LOOP_WORKER_POOL_NAME  = "NettyServerWorker";

    String NETTY_SERVER_PORT = "netty.server.port";

    String SSL_ENABLED_KEY = "ssl-enabled";

    String OS_NAME_KEY = "os.name";

    String OS_LINUX_PREFIX = "linux";

    String OS_WIN_PREFIX = "win";

    String SCHEME_HTTP = "http";

    String SCHEME_WS = "ws";

    String SCHEME_WSS = "wss";

    String HOST_LOCAL = "127.0.0.1";
}
