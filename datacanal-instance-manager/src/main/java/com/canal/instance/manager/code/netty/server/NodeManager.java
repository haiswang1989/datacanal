package com.canal.instance.manager.code.netty.server;

import org.I0Itec.zkclient.ZkClient;

import com.canal.instance.manager.code.netty.handler.ClientChannelInitializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 结点管理
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 上午11:37:17
 */
public class NodeManager {
    
    private Bootstrap bootstap;
    
    private EventLoopGroup group;
    
    private static class SingletonHolder {
        private static final NodeManager NODE_MANAGER_SERVER = new  NodeManager();
    }
    
    public static NodeManager instance() {
        return SingletonHolder.NODE_MANAGER_SERVER;
    }
    
    private NodeManager() {
        bootstap = new Bootstrap();
        group = new NioEventLoopGroup();
        bootstap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true);
    }
    
    public ChannelFuture connect(String ip, int port, String startInstanceShell, ZkClient zkClient) throws InterruptedException {
        bootstap.handler(new ClientChannelInitializer(startInstanceShell, zkClient));
        return bootstap.connect(ip, port).sync();
    }
    
    public void stop() {
        if(null!=group) {
            group.shutdownGracefully();
        }
    }
}
