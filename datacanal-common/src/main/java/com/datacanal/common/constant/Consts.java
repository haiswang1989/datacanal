package com.datacanal.common.constant;

/**
 * 一些常量的定义
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月31日 下午6:02:57
 */
public class Consts {
    
    /**
     * 分布式锁的基础目录
     */
    public static final String DISTRIBUTED_LOCK_BASE_PATH = "/canal/center/distributedlock";
    
    /**
     * canal center分布式锁的创建目录
     */
    public static final String DISTRIBUTED_LOCK_CANAL_CENTER = "cannal_center";
    
    /**
     * zookeeper目录分隔符
     */
    public static final String ZK_PATH_SEPARATOR = "/";
    
    /**
     * center server注册目录
     */
    public static final String ZK_PATH_CENTER_SERVER = "/canal/center/server";
    
    /**
     * 运行作业的结点
     */
    public static final String ZK_PATH_RUNNING_NODE = "/canal/node";
    
    /**
     * 任务目录
     */
    public static final String ZK_PATH_TASK = "/canal/center/task";
    
}
