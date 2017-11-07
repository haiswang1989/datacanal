package com.canal.center.selecter.intf;

import java.util.Map;

import io.netty.channel.Channel;

/**
 * 选择运行instance的结点的选择器
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月6日 下午5:41:07
 */
public interface IChannelSelector {
    
    public Map.Entry<String, Channel> select();
    
}
