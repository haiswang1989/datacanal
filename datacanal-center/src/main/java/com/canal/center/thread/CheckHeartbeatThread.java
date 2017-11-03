package com.canal.center.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.canal.center.cache.ChannelHeartbeatCache;

/**
 * 检查心跳的的线程
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 下午6:21:58
 */
public class CheckHeartbeatThread implements Runnable {
    
    private int maxHeartbeat;
    
    public CheckHeartbeatThread(int maxHeartbeatArg) {
        this.maxHeartbeat = maxHeartbeatArg;
    }
    
    @Override
    public void run() {
        ChannelHeartbeatCache cache = ChannelHeartbeatCache.instance();
        ConcurrentHashMap<String, Long> nodeIdToBeatTime = cache.getNodeIdToBeatTime();
        long currTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : nodeIdToBeatTime.entrySet()) {
            Long lastBeatTime = entry.getValue();
            if(currTime - lastBeatTime > this.maxHeartbeat) {
                //TODO 这边需要进行报警
                System.err.println("node id : " + entry.getKey() + " is missing.");
            }
        }
    }
}
