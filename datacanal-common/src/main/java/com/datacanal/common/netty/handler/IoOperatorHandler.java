package com.datacanal.common.netty.handler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Channel的IO操作的处理类
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 上午11:27:17
 */
public class IoOperatorHandler extends ChannelInitializer<SocketChannel> {
    
    ChannelHandlerAdapter[] channelHandlers;
    
    public IoOperatorHandler(ChannelHandlerAdapter ... channelHandlersArg) {
        this.channelHandlers = channelHandlersArg;
    }
    
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        
        socketChannel.pipeline().addLast(channelHandlers);
        /*
        //解码器
        socketChannel.pipeline().addLast(new HeadWithBodyDecodeHandler());
        //instance启动command的处理类
        socketChannel.pipeline().addLast(new InstanceStartHandler());
        */
    }
}
