package com.canal.center.netty.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * IO事件的处理类
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午2:38:25
 */
public class AcceptorHandler extends ChannelInitializer<SocketChannel> {
    
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new HeadWithBodyDecodeHandler());
        socketChannel.pipeline().addLast(new HeartbeatHandler());
    }
}
