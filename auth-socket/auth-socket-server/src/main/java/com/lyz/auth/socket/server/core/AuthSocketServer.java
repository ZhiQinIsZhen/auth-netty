package com.lyz.auth.socket.server.core;

import com.lyz.auth.common.codec.decode.AuthNettyDecode;
import com.lyz.auth.common.codec.encode.AuthNettyEncode;
import com.lyz.auth.common.codec.handler.AuthChannelHandler;
import com.lyz.auth.common.util.NettyToolUtil;
import com.lyz.auth.common.util.constant.CommonConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.io.File;
import java.security.PrivateKey;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Desc:netty bootstrap
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 16:40
 */
@Slf4j
@Service
public class AuthSocketServer implements InitializingBean, DisposableBean {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel channel;

    private void init() {
        bootstrap = new ServerBootstrap();
        //创建组
        bossGroup = NettyToolUtil.createEventLoopGroup(1, CommonConstant.DEFAULT_BOSS_POOL_NAME, Boolean.TRUE);
        workerGroup = NettyToolUtil.createEventLoopGroup(CommonConstant.DEFAULT_IO_THREADS,
                CommonConstant.DEFAULT_WORKER_POOL_NAME, Boolean.TRUE);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NettyToolUtil.shouldEpoll(Boolean.TRUE) ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws SSLException {
                        if (Boolean.parseBoolean(System.getProperty(CommonConstant.SSL_ENABLED_KEY, Boolean.FALSE.toString()))) {
                            final SslContext sslCtx = SslContextBuilder
                                    .forServer((PrivateKey) new File("C://code"))
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                    .build();
                            socketChannel.pipeline().addLast("negotiation", sslCtx.newHandler(socketChannel.alloc()));
                        }
                        socketChannel.pipeline()
                                .addLast("decoder", new AuthNettyDecode())
                                .addLast("encoder", new AuthNettyEncode())
                                .addLast("server-idle-handler", new IdleStateHandler(0, 0, 15 * 1000, MILLISECONDS))
                                .addLast("handler", new AuthChannelHandler(false));
                    }
                });
    }

    private void open() {
        // bind
        ChannelFuture channelFuture = bootstrap.bind(Integer.parseInt(System.getProperty(CommonConstant.NETTY_SERVER_PORT, "9999")));
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    private void close() {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Throwable e) {
            log.warn("netty channel close fail", e);
        }

        try {
            if (bootstrap != null) {
                long timeout = 2000L;
                long quietPeriod = Math.min(2000L, timeout);
                io.netty.util.concurrent.Future<?> bossGroupShutdownFuture = bossGroup.shutdownGracefully(quietPeriod, timeout, MILLISECONDS);
                Future<?> workerGroupShutdownFuture = workerGroup.shutdownGracefully(quietPeriod, timeout, MILLISECONDS);
                bossGroupShutdownFuture.syncUninterruptibly();
                workerGroupShutdownFuture.syncUninterruptibly();
            }
        } catch (Throwable e) {
            log.warn("netty bootstrap close fail", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        this.close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
        executor.execute(this::open);
    }
}
