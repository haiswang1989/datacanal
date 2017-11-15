package com.canal.instance.manager.code.netty.handler;

import org.I0Itec.zkclient.ZkClient;
import com.datacanal.common.netty.handler.HeadWithBodyDecodeHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Channel的IO操作的处理类
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 上午11:27:17
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    private String startInstanceShell;
    private ZkClient zkClient;
    private String pidPath;
    
    public ClientChannelInitializer(String startInstanceShellArg, ZkClient zkClientArg, String pidPathArg) {
        this.startInstanceShell = startInstanceShellArg;
        this.zkClient = zkClientArg;
        this.pidPath = pidPathArg;
    }
    
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //解码器
        socketChannel.pipeline().addLast(new HeadWithBodyDecodeHandler());
        //instance启动command的处理类
        socketChannel.pipeline().addLast(new InstanceStartHandler(this.startInstanceShell,this.zkClient,this.pidPath));
    }
}
