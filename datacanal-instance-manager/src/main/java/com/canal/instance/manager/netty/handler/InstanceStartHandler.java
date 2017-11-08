package com.canal.instance.manager.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacanal.common.model.Command;
import com.datacanal.common.model.EventType;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理服务端发过来的启动instance的指令
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月6日 下午3:08:01
 */
public class InstanceStartHandler extends ChannelHandlerAdapter {
    
    public static final Logger log = LoggerFactory.getLogger(InstanceStartHandler.class);
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command)msg;
        EventType eventType = command.getEventType();
        if(eventType == EventType.INSTANCE_START) {
            String physicsTable = (String)command.getObj();
            log.info("Get start instance command ,table name : " + physicsTable);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
