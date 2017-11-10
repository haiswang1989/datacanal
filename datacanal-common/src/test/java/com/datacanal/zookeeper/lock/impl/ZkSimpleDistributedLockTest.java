package com.datacanal.zookeeper.lock.impl;

import org.I0Itec.zkclient.ZkClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.datacanal.common.constant.Consts;
import com.datacanal.zookeeper.lock.intf.IDistributedLock;

public class ZkSimpleDistributedLockTest {
    
    private IDistributedLock lock;
    
    protected ZkClient zkClient;
    
    @BeforeClass
    public void setupENV() {
        String lockPath = Consts.DATACANAL_LOCK_SERVERLOCK;
        String zkString = "10.199.188.79:2181,10.199.187.101:2181,10.199.187.102:2181";
        zkClient = new ZkClient(zkString);
        lock = new ZkSimpleDistributedLock(lockPath, zkClient);
    }
    
    @Test
    public void lock() {
        lock.lock();
        System.out.println("Get lock success...");
    }
    
    @Test
    public void unLock() {
        lock.unlock();
    }
    
}
