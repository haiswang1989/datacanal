package com.canal.instance.manager.netty.handler;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.canal.instance.manager.NodeManagerLaucher;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.DbInfo;
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
    
    public static final Logger LOG = LoggerFactory.getLogger(NodeManagerLaucher.class);
    
    //启动脚本所在的位置
    private String startInstanceShell;
    
    //zk
    private ZkClient zkClient;
    
    public InstanceStartHandler(String startInstanceShellArg, ZkClient zkClientArg) {
        this.startInstanceShell = startInstanceShellArg;
        this.zkClient = zkClientArg;
    }
    
    public static final Logger log = LoggerFactory.getLogger(InstanceStartHandler.class);
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command)msg;
        EventType eventType = command.getEventType();
        if(eventType == EventType.INSTANCE_START) {
            String physicsTable = (String)command.getObj();
            log.info("Get start instance command ,table name : " + physicsTable);
            startInstance(physicsTable);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
    
    /**
     * 处理command,启动instance
     * @param physicsTable
     */
    public void startInstance(String physicsTable) {
        if(zkClient.exists(physicsTable)) {
            String jsonString = zkClient.readData(physicsTable);
            DbInfo dbInfo = JSON.parseObject(jsonString, DbInfo.class);
            String cmd = buildCmd(dbInfo);
            LOG.info("Cmd : {}", cmd);
        } else {
            LOG.error("Start instance failed, path : [{}] not exist.", physicsTable);
        }
    }
    
    /**
     * 构造启动脚本
     * @param dbInfo
     */
    public String buildCmd(DbInfo dbInfo) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("sh ").append(startInstanceShell).append(" ").append(" %s %d %s %s %s %s");
        return String.format(cmd.toString(), dbInfo.getHost(), dbInfo.getPort(), dbInfo.getUsername(), 
                dbInfo.getPassword(), dbInfo.getDbName(), StringUtils.join(dbInfo.getSensitiveTables(), ","));
    }
}
