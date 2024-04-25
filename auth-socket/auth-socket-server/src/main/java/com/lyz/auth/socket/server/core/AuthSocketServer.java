package com.lyz.auth.socket.server.core;

import com.lyz.auth.common.codec.decode.AuthNettyDecode;
import com.lyz.auth.common.codec.encode.AuthNettyEncode;
import com.lyz.auth.common.codec.handler.AuthChannelHandler;
import com.lyz.auth.common.util.NettyToolUtil;
import com.lyz.auth.common.util.constant.CommonConstant;
import com.lyz.auth.socket.server.properties.AuthNettyServerProperties;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.io.File;
import java.security.PrivateKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Desc:netty bootstrap
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 16:40
 */
@Slf4j
@Service
@EnableConfigurationProperties(AuthNettyServerProperties.class)
public class AuthSocketServer implements InitializingBean, DisposableBean {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private final AuthNettyServerProperties properties;

    public AuthSocketServer(AuthNettyServerProperties properties) {
        this.properties = properties;
    }

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
                        if (properties.isSslEnabled()) {
                            final SslContext sslCtx = SslContextBuilder
                                    .forServer((PrivateKey) new File(properties.getSslPathName()))
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                    .build();
                            socketChannel.pipeline().addLast("negotiation", sslCtx.newHandler(socketChannel.alloc()));
                        }
                        socketChannel.pipeline()
                                .addLast("decoder", new AuthNettyDecode())
                                .addLast("encoder", new AuthNettyEncode())
                                .addLast("server-idle-handler", new IdleStateHandler(
                                        properties.getReaderIdleTime(),
                                        properties.getWriterIdleTime(),
                                        properties.getAllIdleTime(), SECONDS))
                                .addLast("handler", new AuthChannelHandler(false));
                    }
                });
    }

    private void open() {
        // bind
        ChannelFuture channelFuture = bootstrap.bind(properties.getPort());
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
                long timeout = 300L;
                long quietPeriod = Math.min(properties.getGraceful(), timeout);
                io.netty.util.concurrent.Future<?> bossGroupShutdownFuture = bossGroup.shutdownGracefully(quietPeriod, timeout, SECONDS);
                Future<?> workerGroupShutdownFuture = workerGroup.shutdownGracefully(quietPeriod, timeout, SECONDS);
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
