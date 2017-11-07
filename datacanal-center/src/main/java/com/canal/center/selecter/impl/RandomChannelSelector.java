package com.canal.center.selecter.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.canal.center.cache.ChannelHeartbeatCache;

import io.netty.channel.Channel;

/**
 * 随机channel选择器
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月6日 下午5:45:58
 */
public class RandomChannelSelector extends AbstractChannelSelector {
    
    private Random random;
    
    public RandomChannelSelector() {
        random = new Random();
    }
    
    @Override
    public Map.Entry<String, Channel> select() {
        ConcurrentHashMap<String, Channel> nodeIdToChannel = ChannelHeartbeatCache.instance().getNodeIdToChannel();
        ArrayList<Map.Entry<String, Channel>> channels = new ArrayList<>(nodeIdToChannel.entrySet());
        int length = channels.size();
        if(0 == length) {
            return null;
        }
        
        int index = random.nextInt() % length;
        return channels.get(index);
    }

}

