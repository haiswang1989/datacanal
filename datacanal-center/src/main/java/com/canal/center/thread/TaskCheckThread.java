package com.canal.center.thread;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.canal.center.cache.TaskCache;
import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.DbInfo;
import com.datacanal.common.model.EventType;

/**
 * 检查所有的task
 * 
 * 将不在运行的分片(避免有分片由于种种原因没有被抽取的问题)
 * 
 * 1：这个可能会产生重复的启动COMMAND,但是不影响每个分片只回启动一个instance去处理
 * 2：主要是为了解决Manager启动instance的时候,出现启动失败的情况
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月17日 下午2:42:55
 */
public class TaskCheckThread implements Runnable {
    
    public static final Logger LOG = LoggerFactory.getLogger(TaskCheckThread.class);
    
    private ZkClient zkClient;
    
    public TaskCheckThread(ZkClient zkClientArg) {
        this.zkClient = zkClientArg;
    }
        
    @Override
    public void run() {
        List<String> tasks = zkClient.getChildren(Consts.DATACANAL_TASK);
        StringBuilder fullTaskPath = new StringBuilder();
        for (String taskName : tasks) {
            fullTaskPath.setLength(0);
            fullTaskPath.append(Consts.DATACANAL_TASK).append(Consts.ZK_PATH_SEPARATOR).append(taskName);
            String logicTable = fullTaskPath.toString();
            
            List<String> physicsTables = zkClient.getChildren(logicTable);
            StringBuilder instanceRunningNode = new StringBuilder();
            for (String physicsTable : physicsTables) {
                instanceRunningNode.setLength(0);
                instanceRunningNode.append(logicTable).append(Consts.ZK_PATH_SEPARATOR)
                                .append(physicsTable).append(Consts.ZK_PATH_SEPARATOR).append(Consts.DATACANAL_TASK_INSTANCE);
                
                List<String> runningNodes = zkClient.getChildren(instanceRunningNode.toString());
                //没有node跑该分片,需要启动
                if(0==runningNodes.size()) {
                    String dbInfos = zkClient.readData(physicsTable);
                    DbInfo dbInfo = JSON.parseObject(dbInfos, DbInfo.class);
                    LOG.info("No node running this instance ,need do start. DBInfo : [{}]", dbInfo.toString());
                    
                    //添加新的分片,需要到其他node上启动instance
                    Command command = new Command();
                    command.setEventType(EventType.INSTANCE_START);
                    //传递的是physics的路径,也就是parentPath父亲路径
                    command.setObj(physicsTable);
                    LOG.info("Add command : " + command.toString());
                    TaskCache.instance().getCommands().offer(command);
                }
            }
        }
        
    }
}
