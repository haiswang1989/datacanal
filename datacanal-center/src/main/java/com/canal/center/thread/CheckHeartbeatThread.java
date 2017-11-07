package com.canal.center.thread;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.canal.center.cache.ChannelHeartbeatCache;

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
    
    public CheckHeartbeatThread(int maxHeartbeatArg) {
        this.maxHeartbeat = maxHeartbeatArg;
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
        
        //TODO stop运行在结点上的instance
    }
}
