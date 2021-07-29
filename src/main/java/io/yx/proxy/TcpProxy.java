package io.yx.proxy;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import io.yx.proxy.netty.NettyServer;
import io.yx.proxy.socket.SocketServer;

public class TcpProxy {
    public static final Log log = LogFactory.get();

    public static final Props props = new Props("proxy.properties");


    public static void main(String[] args) {
        NIO();
    }

    public static void BIO() {
        SocketServer socketServer = new SocketServer();

        try {
            socketServer.start();
        } catch (Exception e) {
            log.error("服务启动失败", e);
        }
    }

    public static void NIO() {
        final Integer serverPort = props.getInt("server_port");
        final String upstreamHost = props.getStr("upstream_host");
        final Integer upstreamPort = props.getInt("upstream_port");
        NettyServer nettyServer = new NettyServer(serverPort, upstreamHost, upstreamPort);
        nettyServer.start();
    }

}
