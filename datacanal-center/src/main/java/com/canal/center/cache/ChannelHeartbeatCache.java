package com.canal.center.cache;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import lombok.Getter;

/**
 * 缓存客户端的channel以及心跳信息
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 下午6:01:58
 */
public class ChannelHeartbeatCache {
    
    @Getter
    private ConcurrentHashMap<String, Channel> nodeIdToChannel;
    
    @Getter
    private ConcurrentHashMap<String, Long> nodeIdToBeatTime;
    
    private static final class SingletonHolder {
        private static final ChannelHeartbeatCache CHANNEL_HEARTBEAT_CACHE = new ChannelHeartbeatCache();
    }
    
    public static ChannelHeartbeatCache instance() {
        return SingletonHolder.CHANNEL_HEARTBEAT_CACHE;
    }
    
    private ChannelHeartbeatCache() {
        nodeIdToChannel = new ConcurrentHashMap<>();
        nodeIdToBeatTime = new ConcurrentHashMap<>();
    }
}
