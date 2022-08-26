package io.yx.proxy.netty;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyServer {

    private static final Log log = LogFactory.get();

    private final Integer port;
    private final String upstreamHost;
    private final Integer upstreamPort;

    public NettyServer(Integer port, String upstreamHost, Integer upstreamPort) {
        this.port = port;
        this.upstreamHost = upstreamHost;
        this.upstreamPort = upstreamPort;
    }


    public void start() {
        // 客户端bootstrap
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(clientGroup);
        clientBootstrap.channel(NioSocketChannel.class);

        // 作为服务端的bootstrap
        EventLoopGroup serviceGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(serviceGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.localAddress(port);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel downStreamChannel) throws Exception {
                // 当这里被调用的时候,说明新建了一个连接,这时需要用客户端bootstrap建立一个到上游的连接
                clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel upstreamChannel) throws Exception {
                        // 将下游的连接管道交给构造函数
                        upstreamChannel.pipeline().addLast(new DataHandler(downStreamChannel));
                    }
                });
                // 建立与上游的连接得到channelFuture
                ChannelFuture channelFuture = clientBootstrap.connect(upstreamHost, upstreamPort).sync();
                // 将上游的连接管道交给下游连接的处理器的构造函数
                final Channel upstreamChannel = channelFuture.channel();
                downStreamChannel.pipeline().addLast(new DataHandler(upstreamChannel));
            }
        });
        final ChannelFuture bindFuture = serverBootstrap.bind();
        bindFuture.addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {// 如果绑定失败则输出异常关闭系统
                Throwable cause = f.cause();
                log.error("代理服务启动失败", cause);
                System.exit(0);
            } else {
                log.info("代理服务启动完成 \n本地端口port:{} \n上游主机upstream_host:{} \n上游主机端口upstream_port:{}", port, upstreamHost, upstreamPort);
            }
        });

    }


}
