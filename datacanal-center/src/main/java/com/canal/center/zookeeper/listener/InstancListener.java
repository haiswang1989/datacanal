package com.canal.center.zookeeper.listener;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.canal.center.cache.TaskCache;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.DbInfo;
import com.datacanal.common.model.EventType;

/**
 * 对物理表的运行instance进行监控
 * /canal/center/task/{tablename}/{db instance}/{running node}
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午6:05:30
 */
public class InstancListener implements IZkChildListener {
    
    public static final Logger LOG = LoggerFactory.getLogger(InstancListener.class);
    
    private ZkClient zkClient;
    
    public InstancListener(ZkClient zkClient) {
        this.zkClient = zkClient;
    }
    
    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
        if(!zkClient.exists(parentPath)) {
            //分片直接下线了
            return;
        }
        
        //instance下线了,需要重新在其他机器启动
        if(null==currentChilds || 0==currentChilds.size()) {
            String dbInfos = zkClient.readData(parentPath);
            //目标数据库的信息
            DbInfo dbInfo = JSON.parseObject(dbInfos, DbInfo.class);
            LOG.error("instance is lost, need restart in other node, DBInfo : " + dbInfo.toString());
            
            
            //添加新的分片,需要到其他node上启动instance
            Command command = new Command();
            command.setEventType(EventType.INSTANCE_START);
            //传递的是physics的路径,也就是parentPath父亲路径
            command.setObj(parentPath.substring(0, parentPath.lastIndexOf("/instance")));
            LOG.info("Add command : " + command.toString());
            TaskCache.instance().getCommands().offer(command);
            
        } 
    }
}
