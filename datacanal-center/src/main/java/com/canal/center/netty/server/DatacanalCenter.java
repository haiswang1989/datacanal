package com.canal.center.netty.server;

import com.canal.center.netty.handler.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午2:24:04
 */
public class DatacanalCenter {
    
    //Netty包装了NIO server的辅助类
    private ServerBootstrap serverBootstrap;
    
    //Netty主从多线程模型之mainReactor角色
    private EventLoopGroup bossGroup;
    
    //Netty主从多线程模型之subReactor角色
    private EventLoopGroup workerGroup;
    
    @Setter
    @Getter
    private int port;
    
    /**
     * 单例
     * <p>Description:</p>
     * @author hansen.wang
     * @date 2017年11月1日 下午2:56:26
     */
    private static class SingletonHolder {
        private static final DatacanalCenter DATACANAL_CENTER_SERVER = new DatacanalCenter();
    }
    
    /**
     * 获取实例
     * @return
     */
    public static DatacanalCenter instance() {
        return SingletonHolder.DATACANAL_CENTER_SERVER;
    }
    
    private DatacanalCenter() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .childHandler(new ServerChannelInitializer());
    }
    
    /**
     * 启动服务
     * @param port
     * @throws InterruptedException
     */
    public ChannelFuture start() throws InterruptedException {
        return serverBootstrap.bind(this.port).sync();
    }
    
    /**
     * 关闭资源
     */
    public void stop() {
        if(null!=bossGroup) {
            bossGroup.shutdownGracefully();
        }
        
        if(null!=workerGroup) {
            workerGroup.shutdownGracefully();
        }
    }
}
