package com.lyz.auth.common.util;

import com.lyz.auth.common.util.constant.CommonConstant;
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
     * @param threads 线程数
     * @param poolName 线程池名称
     * @param epoll 是否为epoll模型
     * @return 线程池
     */
    public static EventLoopGroup createEventLoopGroup(int threads, String poolName, boolean epoll) {
        ThreadFactory threadFactory = new DefaultThreadFactory(poolName, Boolean.TRUE);
        return shouldEpoll(epoll) ? new EpollEventLoopGroup(threads, threadFactory) : new NioEventLoopGroup(threads, threadFactory);
    }

    /**
     * 是否是epoll模型
     *
     * @param epoll epoll
     * @return boolean
     */
    public static boolean shouldEpoll(boolean epoll) {
        if (epoll) {
            String osName = System.getProperty(CommonConstant.OS_NAME_KEY);
            return osName.toLowerCase().contains(CommonConstant.OS_LINUX_PREFIX) && Epoll.isAvailable();
        }
        return false;
    }
}
