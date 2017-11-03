package com.canal.center.netty.handler;

import com.canal.center.cache.ChannelHeartbeatCache;
import com.canal.serializer.intf.ISerializer;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.EventType;
import com.datacanal.common.model.Heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午2:43:35
 */
public class HeartbeatHandler extends ChannelHandlerAdapter {
    
    private ISerializer<Heartbeat> serializer;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        
        Command command = (Command)msg;
        EventType eventType = command.getEventType();
        
        /*
        ByteBuf byteBuf = (ByteBuf)msg;
        byte[] requestByte = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(requestByte);
        Heartbeat heartbeat = this.serializer.decode(requestByte, Heartbeat.class);
        
        String nodeId = heartbeat.getNodeId();
        Channel channel = ctx.channel();
        long lastHeartbeatTime = heartbeat.getHeartbeatTime();
        
        //更新缓存中的channel以及心跳信息
        ChannelHeartbeatCache cache = ChannelHeartbeatCache.instance();
        cache.getNodeIdToChannel().put(nodeId, channel);
        cache.getNodeIdToBeatTime().put(nodeId, lastHeartbeatTime);
        */
    } 
}
