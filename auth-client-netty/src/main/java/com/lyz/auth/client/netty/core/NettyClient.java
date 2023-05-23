package com.lyz.auth.client.netty.core;

import com.lyz.auth.common.netty.constant.AuthNettyConstant;
import com.lyz.auth.common.netty.decode.AuthNettyDecode;
import com.lyz.auth.common.netty.encode.AuthNettyEncode;
import com.lyz.auth.common.netty.handler.AuthChannelHandler;
import com.lyz.auth.common.netty.util.NettyToolUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/23 16:09
 */
@Slf4j
@Service
public class NettyClient implements InitializingBean, DisposableBean {

    @Value("${auth.netty.server.address}")
    private String address;

    private Bootstrap bootstrap;
    private EventLoopGroup loopGroup;
    private Channel channel;
    private URI uri;
    private SslContext sslContext;
    private String scheme;
    private String host;
    private int port;

    private void start() throws InterruptedException {
        bootstrap = new Bootstrap();
        loopGroup = NettyToolUtil.createEventLoopGroup(0, "client", Boolean.TRUE);
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
        reConnect();
    }

    private void reConnect() throws InterruptedException {
        if (channel != null && channel.isActive()) {
            return;
        }
        final int port = this.getPortByURI();
        ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            if (channelFuture1.isSuccess()) {
                channel = channelFuture1.channel();
            } else {
                channelFuture1.channel().eventLoop().schedule(() -> {
                    try {
                        reConnect();
                    } catch (Throwable e) {
                        log.error("connection fail", e);
                    }
                }, 10, TimeUnit.SECONDS);
            }
        }).sync();
    }

    /**
     * get port
     *
     * @return
     */
    private int getPortByURI() {
        final int port;
        if (uri.getPort() == -1) {
            if (AuthNettyConstant.SCHEME_HTTP.equalsIgnoreCase(scheme) || AuthNettyConstant.SCHEME_WS.equalsIgnoreCase(scheme)) {
                port = 80;
            } else if (AuthNettyConstant.SCHEME_WSS.equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }
        return port;
    }

    /**
     * 获取ssl上下文
     *
     * @return
     */
    private SslContext getSslContextByURI() throws SSLException {
        final boolean ssl = AuthNettyConstant.SCHEME_WSS.equalsIgnoreCase(scheme);
        final SslContext sslCtx = ssl ? SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build() : null;
        return sslCtx;
    }

    /**
     * 关闭
     */
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
                Future<?> loopGroupShutdownFuture = loopGroup.shutdownGracefully(quietPeriod, timeout, MILLISECONDS);
                loopGroupShutdownFuture.syncUninterruptibly();
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
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("please add netty server address");
        }
        uri = new URI(address);
        scheme = uri.getScheme() == null ? AuthNettyConstant.SCHEME_HTTP : uri.getScheme();
        if (!AuthNettyConstant.SCHEME_WS.equalsIgnoreCase(scheme) && !AuthNettyConstant.SCHEME_WSS.equalsIgnoreCase(scheme)) {
            log.error("Only WS(S) is supported");
            throw new UnsupportedAddressTypeException();
        }
        host = uri.getHost() == null ? AuthNettyConstant.HOST_LOCAL : uri.getHost();
        port = this.getPortByURI();
        sslContext = this.getSslContextByURI();
        new Thread(() -> {
            try {
                start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
