package io.yx.proxy;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import io.yx.proxy.memory.MemoryManagement;
import io.yx.proxy.netty.NettyServer;
import io.yx.proxy.socket.SocketServer;
import io.yx.proxy.vertx.VertxHttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
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
        NIO();
        final Thread memoryMonitor = new Thread(() -> {
            while (true) {
                final String next = scanner.next();
                if ("gc".equalsIgnoreCase(next)) {
                    MemoryManagement.gc();
                }
                MemoryManagement.memoryInfo();
            }
        });
        memoryMonitor.setDaemon(true);
        memoryMonitor.setName("memoryMonitor");
        memoryMonitor.start();
        System.out.println("进程id: " + ManagementFactory.getRuntimeMXBean().getName());

        // 启动http服务供查询cpu和内存信息
        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.listen();
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
