package io.yx.proxy.netty;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * 数据处理程序
 */
public class DataHandler extends ChannelInboundHandlerAdapter {

    private static final Log log = LogFactory.get();

    Channel upstreamChannel;// 到上游的管道连接

    public DataHandler(Channel upstreamChannel) {
        this.upstreamChannel = upstreamChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        log.info("GET DATA:{}", byteBuf.toString(StandardCharsets.UTF_8));
        byteBuf.retain();
        upstreamChannel.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        upstreamChannel.close();
        ctx.close();
        log.error("连接异常", cause);
    }

    /**
     * 当从 ChannelPipeline 中移除 ChannelHandler 时被调用
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        upstreamChannel.close();
        log.info("handlerRemoved关闭下游管道");
        ctx.close();
    }
}
