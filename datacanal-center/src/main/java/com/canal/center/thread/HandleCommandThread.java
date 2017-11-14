package com.canal.center.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.canal.center.cache.ChannelHeartbeatCache;
import com.canal.center.cache.TaskCache;
import com.canal.center.selecter.intf.IChannelSelector;
import com.canal.serializer.intf.ISerializer;
import com.datacanal.common.model.Command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * 处理command的线程
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月6日 下午5:30:08
 */
public class HandleCommandThread implements Runnable {
    
    public static final Logger LOG = LoggerFactory.getLogger(HandleCommandThread.class);
    
    private IChannelSelector selector;
    
    private ISerializer<Command> serializer;
    
    public HandleCommandThread(IChannelSelector selectorArg, ISerializer<Command> serializerArg) {
        this.selector = selectorArg;
        this.serializer = serializerArg;
    }
    
    @Override
    public void run() {
        
        LinkedBlockingQueue<Command> commands = TaskCache.instance().getCommands();
        Command command = null;
        try {
            while(true) {
                command = commands.take();
                handleCommand(command);
            }
        } catch (InterruptedException e) {
        }
    }
    
    /**
     * 处理command指令
     * @param command
     */
    public void handleCommand(Command command) {
        String nodeId = null;
        //如果没有可用的node就一直循环测试
        while(true) {
            if(null!=(nodeId=selector.select())) {
                Channel channel = ChannelHeartbeatCache.instance().getNodeIdToChannel().get(nodeId);
                //解决在被select到的同时被"因为心跳断开"而被清除的情况而导致的问题
                if(null==channel) {
                    continue;
                }
                
                LOG.info("Instance start, command : " + command + ", nodeid : " + nodeId);
                byte[] body = serializer.encode(command);
                int bodyLength = body.length;
                ByteBuf writeBuf = Unpooled.buffer(32 + bodyLength);
                //发送流前面记录body的长度(一个int型存储这个长度)
                writeBuf.writeInt(bodyLength);
                writeBuf.writeBytes(body);
                //发送心跳消息
                channel.writeAndFlush(writeBuf);
                break;
            } 
            
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
            }
        }
    }
}
