package com.datacanal.common.constant;

/**
 * 一些常量的定义
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月31日 下午6:02:57
 */
public class Consts {
    
    //canal 锁的基础目录
    public static final String DATACANAL_LOCK = "/datacanal/lock";
    
    //canal server的分布式锁的目录
    public static final String DATACANAL_LOCK_SERVERLOCK = "/datacanal/lock/serverlock";
    
    //center server注册目录
    public static final String DATACANAL_CANAL_SERVER = "/datacanal/canal_server";
    
    //ZK上分隔符
    public static final String ZK_PATH_SEPARATOR = "/";
    
    //运行作业的结点
    public static final String DATACANAL_NODE = "/datacanal/node";
    
    //任务目录
    public static final String DATACANAL_TASK = "/datacanal/task";
    
    //instance运行的目录
    public static final String DATACANAL_TASK_INSTANCE = "instance";
    
    //
    public static final String DATACANAL_TASK_POSITION = "position";
    
    //position的最小值
    public static final long EARLIEST_POSITION = 4L;
    
}
