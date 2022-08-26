package io.yx.proxy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.Pump;
import io.yx.proxy.netty.NettyServer;
import io.yx.proxy.socket.ProxySocketServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class TcpProxy {

    private static final String confName = "proxy.properties";

    public static final Log log = LogFactory.get();

    public static final Props props = new Props("proxy.properties");

    static {
        File file = new File("");
        try {
            final String canonicalPath = file.getCanonicalPath();
            System.out.println("系统目录：" + canonicalPath);
            // 读取系统目录下的配置文件
            File configFile = new File(canonicalPath + File.separator + confName);
            if (!configFile.exists()) {
                final boolean newFile = configFile.createNewFile();
                if (newFile) {
                    try (final FileOutputStream fileOutputStream = new FileOutputStream(configFile)) {
                        props.store(fileOutputStream, "The configuration file of the proxy server, you can set the ip and port of the upstream server here");
                    }
                } else {
                    log.error("文件不存在，但创建文件失败！{}", configFile.getPath());
                    System.exit(0);
                }
            } else {
                props.load(Files.newInputStream(configFile.toPath()));
            }
        } catch (IOException e) {
            log.error("载入配置文件失败：", e);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        String proxyMode = props.getStr("proxyMode","vertx");
        log.info("代理方式为(proxyMode):{}",proxyMode);

        if (StrUtil.equalsIgnoreCase("vertx", proxyMode)) {
            VERTX();
        } else if (StrUtil.equalsIgnoreCase("netty", proxyMode)) {
            NETTY();
        } else if (StrUtil.equalsIgnoreCase("socket", proxyMode)) {
            BIO();
        } else {
            log.error("没有设置代理模式,请设置一种代理模式 proxyMode:可选的值为[vertx,netty,socket]");
        }
    }

    public static void BIO() {
        ProxySocketServer proxySocketServer = new ProxySocketServer();
        try {
            proxySocketServer.start();
        } catch (Exception e) {
            log.error("服务启动失败", e);
        }
    }

    public static void NETTY() {
        final Integer serverPort = props.getInt("server_port");
        final String upstreamHost = props.getStr("upstream_host");
        final Integer upstreamPort = props.getInt("upstream_port");
        NettyServer nettyServer = new NettyServer(serverPort, upstreamHost, upstreamPort);
        nettyServer.start();
    }

    public static void VERTX() {
        final Integer serverPort = props.getInt("server_port");
        final String upstreamHost = props.getStr("upstream_host");
        final Integer upstreamPort = props.getInt("upstream_port");
        Vertx vertx = Vertx.vertx();
        NetServer netServer = vertx.createNetServer();
        netServer.connectHandler(clientSocket -> {
            log.info("客户端 {} ,创建连接", clientSocket.remoteAddress());
            NetClient proxyClient = vertx.createNetClient();
            SocketAddress upstreamAddress = SocketAddress.inetSocketAddress(upstreamPort, upstreamHost);
            proxyClient.connect(upstreamAddress, result -> {
                if (result.succeeded()) {
                    NetSocket proxySocket = result.result();
                    log.info("代理连接成功 {} <---> {} <---> {}", clientSocket.remoteAddress(), proxySocket.localAddress(), upstreamAddress);
                    Pump.pump(clientSocket, proxySocket).start();
                    Pump.pump(proxySocket, clientSocket).start();
                    proxySocket.closeHandler(event -> {
                        log.info("代理连接关闭");
                        clientSocket.close();// 当代理链接关闭的时候,关闭对应的下游客户端
                    });
                } else {
                    log.error("代理连接上游服务器失败", result.cause());
                }
                clientSocket.closeHandler(event -> log.info("客户端 {} 断开连接", clientSocket.remoteAddress()));
            });
        });
        netServer.listen(serverPort).onComplete(e -> {
            if (e.failed()) {
                log.error("代理服务启动失败", e.cause());
                System.exit(0);
            }
            log.info("开始监听:{}", serverPort);
        });

    }

}
