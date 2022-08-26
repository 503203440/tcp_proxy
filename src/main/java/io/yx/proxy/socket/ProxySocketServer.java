package io.yx.proxy.socket;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.yx.proxy.TcpProxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 接收下游设备连接
 */
public class ProxySocketServer {

    public static final Log log = LogFactory.get();


    private final int port = TcpProxy.props.getInt("server_port");
    private final String upstreamHost = TcpProxy.props.getStr("upstream_host");
    private final int upstreamPort = TcpProxy.props.getInt("upstream_port");


    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void start() throws Exception {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("代理程序启动完成,port:{}", port);
            while (true) {
                Socket downstream = serverSocket.accept();
                downstream.setKeepAlive(true);
                Socket upstream = new Socket(upstreamHost, upstreamPort);
                executorService.submit(new SocketHandler(downstream, upstream));
                executorService.submit(new SocketHandler(upstream, downstream));
            }

        }

    }


}

class SocketHandler implements Runnable {

    private final Socket readSocket;
    private final Socket writeSocket;

    public SocketHandler(Socket readSocket, Socket writeSocket) {
        this.readSocket = readSocket;
        this.writeSocket = writeSocket;
    }

    @Override
    public void run() {
        try (InputStream inputStream = readSocket.getInputStream();
             OutputStream outputStream = writeSocket.getOutputStream();) {
            byte[] datas = new byte[1024];
            int len;
            while ((len = inputStream.read(datas)) != -1) {
                if (len > 0) {
                    outputStream.write(datas, 0, len);
                    outputStream.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
