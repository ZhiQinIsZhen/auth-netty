package com.lyz.auth.websocket.server.core;

import com.lyz.auth.common.util.NettyToolUtil;
import com.lyz.auth.common.util.constant.CommonConstant;
import com.lyz.auth.websocket.server.core.handler.AuthWebsocketChannelHandler;
import com.lyz.auth.websocket.server.decode.MsgPackDecoder;
import com.lyz.auth.websocket.server.decode.WebSocketDecoder;
import com.lyz.auth.websocket.server.encode.MsgPackEncoder;
import com.lyz.auth.websocket.server.encode.WebSocketEncoder;
import com.lyz.auth.websocket.server.properties.AuthNettyServerProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
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
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/27 16:34
 */
@Slf4j
@Service
@EnableConfigurationProperties(AuthNettyServerProperties.class)
public class AuthWebsocketServer implements InitializingBean, DisposableBean {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private final AuthNettyServerProperties properties;

    public AuthWebsocketServer(AuthNettyServerProperties properties) {
        this.properties = properties;
    }

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel channel;

    private void open() {
        bootstrap = new ServerBootstrap();
        //创建组
        bossGroup = NettyToolUtil.createEventLoopGroup(1, CommonConstant.DEFAULT_BOSS_POOL_NAME, Boolean.TRUE);
        workerGroup = NettyToolUtil.createEventLoopGroup(CommonConstant.DEFAULT_IO_THREADS, CommonConstant.DEFAULT_WORKER_POOL_NAME, Boolean.TRUE);
        final WriteBufferWaterMark write = new WriteBufferWaterMark(512 * 1024, 1024 * 1024);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NettyToolUtil.shouldEpoll(Boolean.TRUE) ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .option(ChannelOption.SO_RCVBUF, 64 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, write)
                .childOption(ChannelOption.SO_SNDBUF, 64 * 1024)
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
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                            socketChannel.pipeline().addLast("negotiation", sslCtx.newHandler(socketChannel.alloc()));
                        }
                        socketChannel.pipeline()
                                .addLast("codec-http", new HttpServerCodec())
                                .addLast("aggregator", new HttpObjectAggregator(65536))
                                .addLast("web-socket", new WebSocketServerProtocolHandler(properties.getWebsocketPath(), null, true))
                                .addLast("server-idle-handler", new IdleStateHandler(
                                        properties.getReaderIdleTime(),
                                        properties.getWriterIdleTime(),
                                        properties.getAllIdleTime(),
                                        SECONDS))
                                .addLast("web-socket-decoder", new WebSocketDecoder())
                                .addLast("web-socket-encoder", new WebSocketEncoder())
                                .addLast("decoder", new MsgPackDecoder())
                                .addLast("encoder", new MsgPackEncoder())

                                .addLast("handler", new AuthWebsocketChannelHandler(false));
                    }
                });
        // bind
        ChannelFuture channelFuture = bootstrap.bind(properties.getPort());
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
                long timeout = 300L;
                long quietPeriod = Math.min(properties.getGraceful(), timeout);
                Future<?> bossGroupShutdownFuture = bossGroup.shutdownGracefully(quietPeriod, timeout, SECONDS);
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
        close();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executor.execute(this::open);
    }
}
