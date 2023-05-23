package com.lyz.auth.server.netty.server;

import com.lyz.auth.common.netty.constant.AuthNettyConstant;
import com.lyz.auth.common.netty.decode.AuthNettyDecode;
import com.lyz.auth.common.netty.encode.AuthNettyEncode;
import com.lyz.auth.common.netty.handler.AuthChannelHandler;
import com.lyz.auth.common.netty.util.NettyToolUtil;
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * DESC:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/19 23:02
 */
@Slf4j
@Service
public class NettyServer implements InitializingBean, DisposableBean {

    private ServerBootstrap bootstrap;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel channel;

    /**
     * 开启一个netty server
     *
     */
    private void open() {
        bootstrap = new ServerBootstrap();
        //创建组
        bossGroup = NettyToolUtil.createEventLoopGroup(1, AuthNettyConstant.EVENT_LOOP_BOSS_POOL_NAME, Boolean.TRUE);
        workerGroup = NettyToolUtil.createEventLoopGroup(AuthNettyConstant.DEFAULT_IO_THREADS, AuthNettyConstant.EVENT_LOOP_WORKER_POOL_NAME, Boolean.TRUE);
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
                        if (Boolean.parseBoolean(System.getProperty(AuthNettyConstant.SSL_ENABLED_KEY, "false"))) {
                            final SslContext sslCtx = SslContextBuilder.forServer((PrivateKey) new File("C://code")).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                            socketChannel.pipeline().addLast("negotiation", sslCtx.newHandler(socketChannel.alloc()));
                        }
                        socketChannel.pipeline()
                                .addLast("decoder", new AuthNettyDecode())
                                .addLast("encoder", new AuthNettyEncode())
                                .addLast("server-idle-handler", new IdleStateHandler(0, 0, 15 * 1000, MILLISECONDS))
                                .addLast("handler", new AuthChannelHandler(false));
                    }
                });
        // bind
        ChannelFuture channelFuture = bootstrap.bind(getPort());
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    private void close() {
        try {
            if (channel != null) {
                // unbind.
                channel.close();
            }
        } catch (Throwable e) {
            log.warn("netty channel close fail", e);
        }

        try {
            if (bootstrap != null) {
                long timeout = 2000L;
                long quietPeriod = Math.min(2000L, timeout);
                Future<?> bossGroupShutdownFuture = bossGroup.shutdownGracefully(quietPeriod, timeout, MILLISECONDS);
                Future<?> workerGroupShutdownFuture = workerGroup.shutdownGracefully(quietPeriod, timeout, MILLISECONDS);
                bossGroupShutdownFuture.syncUninterruptibly();
                workerGroupShutdownFuture.syncUninterruptibly();
            }
        } catch (Throwable e) {
            log.warn("netty bootstrap close fail", e);
        }
    }

    /**
     * 获取port
     *
     * @return
     */
    private int getPort() {
        return Integer.parseInt(System.getProperty(AuthNettyConstant.NETTY_SERVER_PORT, "9999"));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            open();
        }).start();
    }

    @Override
    public void destroy() throws Exception {
        close();
    }


}
