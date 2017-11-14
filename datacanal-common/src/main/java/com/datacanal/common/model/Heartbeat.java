package com.datacanal.common.model;

import lombok.Data;

/**
 * 心跳检测的消息
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 下午12:01:00
 */
@Data
public class Heartbeat {
    
    //当前时间
    private long heartbeatTime;
    
    //结点编号
    private String nodeId;
    
    //当前机器的cpu资源
    private CpuResource cpuResource;
    
    //当前机器的内存资源
    private MemoryResource memoryResource;
    
    //当前机器的网络资源
    private NetworkResource networkResource;
}
