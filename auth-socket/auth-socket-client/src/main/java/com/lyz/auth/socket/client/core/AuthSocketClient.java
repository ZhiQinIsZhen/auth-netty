package com.lyz.auth.socket.client.core;

import com.lyz.auth.common.codec.decode.AuthNettyDecode;
import com.lyz.auth.common.codec.encode.AuthNettyEncode;
import com.lyz.auth.common.codec.handler.AuthChannelHandler;
import com.lyz.auth.common.util.NettyToolUtil;
import com.lyz.auth.common.util.constant.CommonConstant;
import com.lyz.auth.socket.client.properties.AuthNettyServerProperties;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Desc:client bootstrap
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 17:31
 */
@Slf4j
@Service
@EnableConfigurationProperties(AuthNettyServerProperties.class)
public class AuthSocketClient implements InitializingBean, DisposableBean {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private final AuthNettyServerProperties properties;

    public AuthSocketClient(AuthNettyServerProperties properties) {
        this.properties = properties;
    }

    private Bootstrap bootstrap;
    private EventLoopGroup loopGroup;
    private Channel channel;
    private URI uri;
    private SslContext sslContext;
    private String scheme;
    private String host;
    private int port;

    private void init() {
        bootstrap = new Bootstrap();
        loopGroup = NettyToolUtil.createEventLoopGroup(0, CommonConstant.DEFAULT_CLIENT_POOL_NAME, Boolean.TRUE);
        bootstrap.group(loopGroup)
                .channel(NettyToolUtil.shouldEpoll(Boolean.TRUE) ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if (sslContext != null) {
                            pipeline.addLast(sslContext.newHandler(socketChannel.alloc(), host, port));
                        }
                        pipeline.addLast("decoder", new AuthNettyDecode())
                                .addLast("encoder", new AuthNettyEncode())
                                .addLast("handler", new AuthChannelHandler(true));
                    }
                });
    }

    private void connect() {
        if (channel != null && channel.isActive()) {
            channel.eventLoop().schedule(this::reconnect, properties.getDelay(), TimeUnit.SECONDS);
            return;
        }
        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
        channel = channelFuture.channel();
        channelFuture.addListener((ChannelFutureListener) listener -> {
                /*if (listener.isSuccess()) {
                    log.info("channel connect success");
                } else {
                    log.error("channel connect failed");
                }*/
            channel.eventLoop().schedule(this::reconnect, properties.getDelay(), TimeUnit.SECONDS);
        });
        try {
            channelFuture.sync();
        } catch (Exception e) {
            log.error("connect fail", e);
            throw new RuntimeException(e);
        }
    }

    public void reconnect() {
        Future future = executor.submit(this::connect);
    }

    /**
     * get port
     *
     * @return port
     */
    private int getPortByURI() {
        final int port;
        if (uri.getPort() == -1) {
            if (CommonConstant.SCHEME_HTTP.equalsIgnoreCase(scheme) || CommonConstant.SCHEME_WS.equalsIgnoreCase(scheme)) {
                port = 80;
            } else if (CommonConstant.SCHEME_WSS.equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }
        return port;
    }

    @Override
    public void destroy() throws Exception {
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
                io.netty.util.concurrent.Future<?> loopGroupShutdownFuture = loopGroup.shutdownGracefully(quietPeriod, timeout, SECONDS);
                loopGroupShutdownFuture.syncUninterruptibly();
            }
        } catch (Throwable e) {
            log.warn("netty bootstrap close fail", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasText(properties.getUrl())) {
            throw new IllegalArgumentException("please add netty server address");
        }
        uri = new URI(properties.getUrl());
        scheme = uri.getScheme() == null ? CommonConstant.SCHEME_HTTP : uri.getScheme();
        if (!CommonConstant.SCHEME_WS.equalsIgnoreCase(scheme) && !CommonConstant.SCHEME_WSS.equalsIgnoreCase(scheme)) {
            log.error("Only WS(S) is supported");
            throw new UnsupportedAddressTypeException();
        }
        host = uri.getHost() == null ? CommonConstant.HOST_LOCAL : uri.getHost();
        port = this.getPortByURI();
        boolean ssl = CommonConstant.SCHEME_WSS.equalsIgnoreCase(scheme);
        sslContext = ssl ? SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build() : null;
        this.init();
        this.reconnect();
    }
}
