package com.canal.center.netty.handler;

import com.datacanal.common.netty.handler.HeadWithBodyDecodeHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Channel的IO操作的处理类
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 上午11:27:17
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    public ServerChannelInitializer() {
    }
    
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new HeadWithBodyDecodeHandler());
        socketChannel.pipeline().addLast(new HeartbeatHandler());
    }
}
