package com.lyz.auth.common.netty.util;

import com.lyz.auth.common.netty.constant.AuthNettyConstant;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadFactory;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/21 14:59
 */
@UtilityClass
public class NettyToolUtil {

    /**
     * 创建netty线程池
     *
     * @param threads
     * @param poolName
     * @param epoll
     * @return
     */
    public static EventLoopGroup createEventLoopGroup(int threads, String poolName, boolean epoll) {
        ThreadFactory threadFactory = new DefaultThreadFactory(poolName, Boolean.TRUE);
        return shouldEpoll(epoll) ? new EpollEventLoopGroup(threads, threadFactory) : new NioEventLoopGroup(threads, threadFactory);
    }

    /**
     * 是否是epoll模型
     *
     * @param epoll
     * @return
     */
    public static boolean shouldEpoll(boolean epoll) {
        if (epoll) {
            String osName = System.getProperty(AuthNettyConstant.OS_NAME_KEY);
            return osName.toLowerCase().contains(AuthNettyConstant.OS_LINUX_PREFIX) && Epoll.isAvailable();
        }
        return false;
    }
}
