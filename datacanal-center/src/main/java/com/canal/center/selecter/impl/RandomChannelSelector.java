package com.canal.center.selecter.impl;

import java.util.HashSet;
import java.util.Random;
import com.canal.center.cache.ChannelHeartbeatCache;

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
    public String select() {
        HashSet<String> nodeIds = ChannelHeartbeatCache.instance().getOnlineNodes();
        int onlineNodesCnt = nodeIds.size();
        if(0 == onlineNodesCnt) {
            return null;
        }
        
        int index = random.nextInt() % onlineNodesCnt;
        return nodeIds.toArray(new String[0])[index];
    }
}

