package io.yx.proxy;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.streams.Pump;
import io.yx.proxy.netty.NettyServer;
import io.yx.proxy.socket.SocketServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class TcpProxy {

    private static final String confName = "proxy.properties";

    public static final Log log = LogFactory.get();

    public static final Props props = new Props("proxy.properties");

    private static final Scanner scanner = new Scanner(System.in);

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
                    System.exit(2);
                }
            } else {
                props.load(new FileInputStream(configFile));
            }
        } catch (IOException e) {
            log.error("载入配置文件失败：", e);
            System.exit(2);
        }
    }

    public static void main(String[] args) {
        NETTY();
    }

    public static void BIO() {
        SocketServer socketServer = new SocketServer();

        try {
            socketServer.start();
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

        NetServer netServer = Vertx.vertx().createNetServer();
        NetClient netClient = Vertx.vertx().createNetClient();
        netServer.connectHandler(clientSocket -> {
            log.info("客户端 {}:{} 创建连接", clientSocket.remoteAddress().host(), clientSocket.remoteAddress().port());
            netClient.connect(upstreamPort, upstreamHost, result -> {
                if (result.succeeded()) {
                    NetSocket proxySocket = result.result();
                    log.info("代理连接成功");
                    Pump.pump(clientSocket, proxySocket).start();
                    Pump.pump(proxySocket, clientSocket).start();
                    proxySocket.closeHandler(event -> log.info("代理连接关闭"));
                } else {
                    log.error("代理连接失败");
                }
                clientSocket.closeHandler(event -> log.info("客户端 {}:{} 断开连接", clientSocket.remoteAddress().host(), clientSocket.remoteAddress().port()));
            });
        });
        log.info("开始监听:{}", serverPort);
        netServer.listen(serverPort);

    }

}
