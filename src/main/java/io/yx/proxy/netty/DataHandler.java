package io.yx.proxy.netty;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("[channelActive] upstreamChannel:{} downstreamChannel:{}", upstreamChannel.remoteAddress(), ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
//        byteBuf.retain();
        upstreamChannel.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        upstreamChannel.close();
        ctx.close();
        log.error("[exceptionCaught]", cause);
    }

    /**
     * 当从 ChannelPipeline 中移除 ChannelHandler 时被调用
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        upstreamChannel.close();
        ctx.close();
        log.info("[handlerRemoved] upstreamChannel:{} downstreamChannel:{}", upstreamChannel.remoteAddress(), ctx.channel().remoteAddress());
    }
}
