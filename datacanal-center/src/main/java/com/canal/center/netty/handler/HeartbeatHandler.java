package com.canal.center.netty.handler;

import com.alibaba.fastjson.JSON;
import com.canal.center.cache.ChannelHeartbeatCache;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.EventType;
import com.datacanal.common.model.Heartbeat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 心跳处理类
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午2:43:35
 */
public class HeartbeatHandler extends ChannelHandlerAdapter {
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command)msg;
        EventType eventType = command.getEventType();
        
        if(eventType == EventType.HEART_BEAT) {
            String jsonString = JSON.toJSONString(command.getObj()); 
            Heartbeat heartbeat = JSON.parseObject(jsonString, Heartbeat.class);
            
            //打印心跳信息
            System.out.println("Heart beat," + heartbeat.toString());
            
            String nodeId = heartbeat.getNodeId();
            Channel channel = ctx.channel();
            long lastHeartbeatTime = heartbeat.getHeartbeatTime();
            
            //更新缓存中的channel以及心跳信息
            ChannelHeartbeatCache cache = ChannelHeartbeatCache.instance();
            cache.getNodeIdToChannel().put(nodeId, channel);
            cache.getNodeIdToBeatTime().put(nodeId, lastHeartbeatTime);
        } else {
            ctx.fireChannelRead(msg);
        }
    } 
}
