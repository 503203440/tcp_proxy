package io.yx.proxy.vertx;

import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.yx.proxy.TcpProxy;
import oshi.hardware.GlobalMemory;

import java.text.DecimalFormat;

/**
 * vertx的http服务
 */
public class VertxHttpServer extends AbstractVerticle {

    private static final Log log = LogFactory.get();

    public int httpPort = TcpProxy.props.getInt("vertx_port", 8080);

    public void listen() {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(VertxHttpServer.class.getName()).onFailure(Throwable::printStackTrace);
        log.info("vertx_port: {}", this.httpPort);
    }

    @Override
    public void start() throws Exception {
        vertx.createHttpServer().requestHandler(request -> {

            // 获取系统内存信息
            final CpuInfo cpuInfo = OshiUtil.getCpuInfo();
            final GlobalMemory memory = OshiUtil.getMemory();

            JSONObject jsonObject = new JSONObject();
            jsonObject.append("CPU系统使用率", cpuInfo.getSys());
            jsonObject.append("CPU用户使用率", cpuInfo.getUsed());
            jsonObject.append("MEM使用率", new DecimalFormat("###.00").format((memory.getTotal() - memory.getAvailable()) * 100 / memory.getTotal()));

            request.response().putHeader("content-type", "application/json")
                    .end(jsonObject.toString());

        }).listen(this.httpPort);
    }

}
