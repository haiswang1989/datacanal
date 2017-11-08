package com.canal.instance.manager.thread;

import com.canal.serializer.intf.ISerializer;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.EventType;
import com.datacanal.common.model.Heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * 发送心跳的线程
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 下午4:04:44
 */
public class HeartbeatThread implements Runnable {
    
    //与服务端(canal server)的连接通道
    private Channel channel;
    
    //序列化实现类
    ISerializer<Command> serializer;
    
    //当前结点的nodeid
    private String nodeId;
    
    public HeartbeatThread(Channel channelArg, ISerializer<Command> serializerArg, String nodeIdArg) {
        this.channel = channelArg;
        this.serializer = serializerArg;
        this.nodeId = nodeIdArg;
    }
    
    @Override
    public void run() {
        Heartbeat heartbeat = new Heartbeat();
        heartbeat.setNodeId(this.nodeId);
        heartbeat.setHeartbeatTime(System.currentTimeMillis());
        //构造成一个command
        Command command = new Command();
        command.setEventType(EventType.HEART_BEAT);
        command.setObj(heartbeat);
        //将command序列化
        byte[] body = serializer.encode(command);
        int bodyLength = body.length;
        ByteBuf writeBuf = Unpooled.buffer(32 + bodyLength);
        //发送流前面记录body的长度(一个int型存储这个长度)
        writeBuf.writeInt(bodyLength);
        writeBuf.writeBytes(body);
        //发送心跳消息
        this.channel.writeAndFlush(writeBuf);
    }
}
