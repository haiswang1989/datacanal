package com.canal.center.thread;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.canal.center.cache.ChannelHeartbeatCache;
import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.Status;

import io.netty.channel.Channel;

/**
 * 检查心跳的的线程
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 下午6:21:58
 */
public class CheckHeartbeatThread implements Runnable {
    
    public static final Logger LOG = LoggerFactory.getLogger(HandleCommandThread.class);
    
    private int maxHeartbeat;
    
    private ZkClient zkClient;
    
    public CheckHeartbeatThread(int maxHeartbeatArg, ZkClient zkClientArg) {
        this.maxHeartbeat = maxHeartbeatArg;
        this.zkClient = zkClientArg;
    }
    
    @Override
    public void run() {
        ChannelHeartbeatCache cache = ChannelHeartbeatCache.instance();
        ConcurrentHashMap<String, Long> nodeIdToBeatTime = cache.getNodeIdToBeatTime();
        
        long currTime = System.currentTimeMillis();
        
        HashSet<String> lostNode = new HashSet<>();
        
        for (Map.Entry<String, Long> entry : nodeIdToBeatTime.entrySet()) {
            Long lastBeatTime = entry.getValue();
            if(currTime - lastBeatTime > this.maxHeartbeat) {
                LOG.warn("node id : " + entry.getKey() + " is missing.");
                lostNode.add(entry.getKey());
            }
        }
        
        //结点丢失,清除服务端缓存的channel
        if(0!=lostNode.size()) {
            ConcurrentHashMap<String, Channel> nodeIdToChannel = cache.getNodeIdToChannel();
            HashSet<String> onlineNodes = cache.getOnlineNodes();
            for (String nodeId : lostNode) {
                nodeIdToBeatTime.remove(nodeId);
                nodeIdToChannel.remove(nodeId);
                onlineNodes.remove(nodeId);
            }
        }
        
        //stop运行在结点上的instance
        for (String nodeId : lostNode) {
            stopNodeInstance(nodeId);
        }
    }
    
    /**
     * 
     * @param nodeId
     */
    public void stopNodeInstance(String nodeId) {
        List<String> logicTables = zkClient.getChildren(Consts.DATACANAL_TASK);
        StringBuilder fullLogicTable = new StringBuilder();
        for (String logicTable : logicTables) {
            fullLogicTable.setLength(0);
            fullLogicTable.append(Consts.DATACANAL_TASK).append(Consts.ZK_PATH_SEPARATOR).append(logicTable);
            List<String> physicsTables = zkClient.getChildren(fullLogicTable.toString());
            StringBuilder fullPhysicsTableInstance = new StringBuilder();
            for (String physicsTable : physicsTables) {
                fullPhysicsTableInstance.setLength(0);
                fullPhysicsTableInstance.append(fullLogicTable.toString())
                    .append(Consts.ZK_PATH_SEPARATOR).append(physicsTable)
                    .append(Consts.ZK_PATH_SEPARATOR).append(Consts.DATACANAL_TASK_INSTANCE);
                
                List<String> instanceNodeId = zkClient.getChildren(fullPhysicsTableInstance.toString());
                if(null!=instanceNodeId && 0!=instanceNodeId.size()) {
                    String currNodeId = instanceNodeId.get(0);
                    if(nodeId.equals(currNodeId)) {
                        //该分片运行在挂掉的结点上
                        String targetPath = fullPhysicsTableInstance.toString() + Consts.ZK_PATH_SEPARATOR + currNodeId;
                        //让其停止
                        zkClient.writeData(targetPath, Status.STOP);
                    }
                }
            }
        }
    }
}
