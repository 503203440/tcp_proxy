package io.yx.proxy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import io.yx.proxy.netty.NettyServer;
import io.yx.proxy.socket.SocketServer;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Scanner;

public class TcpProxy {
    public static final Log log = LogFactory.get();

    public static final Props props = new Props("proxy.properties");

    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        final long pid = ManagementFactory.getRuntimeMXBean().getPid();
        System.out.println("PID:" + pid);

        Thread thread = new Thread(() -> {
            while (true) {
                final String next = in.next();
                if (StrUtil.isNotBlank(next)) {
                    if ("gc".equalsIgnoreCase(next)) {
                        System.gc();
                    }
                    // 输出一次内存信息
                    final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                    // 堆内存的使用
                    final MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
                    System.out.println("堆内存使用量" + ByteFormatUtil.memoryUsageToString(heapMemoryUsage));


                    final MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
                    System.out.println("非堆内存使用量:" + ByteFormatUtil.memoryUsageToString(nonHeapMemoryUsage));

                    final long totalUsed = heapMemoryUsage.getUsed() + nonHeapMemoryUsage.getUsed();
                    System.out.println("程序总共使用内存:" + ByteFormatUtil.formatSize(totalUsed));
                }
            }
        });
        thread.setName("内存使用信息交互线程");
        thread.start();

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
