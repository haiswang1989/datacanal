package com.datacanal.zookeeper.lock.impl;

import java.util.concurrent.CountDownLatch;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import com.datacanal.zookeeper.lock.listener.UnLockListener;

/**
 * 基于Zookeeper的一个简单的分布式锁
 * 
 * 该方式实现分布式锁存在"羊群效应"
 * 但是争抢锁的线程不多的情况下,问题不大
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月31日 下午5:40:50
 */
public class ZkSimpleDistributedLock extends AbstractDistributedLock {
    
    private CountDownLatch latch = null;
    
    //锁的目录
    private String lockPath;
    
    //zk结点的值,也是判断锁是否是该对象拥有的标志
    private int zkNodeVal;
    
    //zookeeper的连接
    private ZkClient zkClient;
    
    public ZkSimpleDistributedLock(String lockPathArg, ZkClient zkClientArg) {
        this.lockPath = lockPathArg;
        this.zkClient = zkClientArg;
        zkNodeVal = this.hashCode();
    }
    
    @Override
    public void lock() {
        while(true) {
            try {
                zkClient.create(lockPath, zkNodeVal, CreateMode.EPHEMERAL);
                //目录创建成功,获取锁成功,break
                break;
            } catch(ZkNodeExistsException ex) {
                latch = new CountDownLatch(1);
                zkClient.subscribeChildChanges(lockPath, new UnLockListener(latch));
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    //ignore
                }
            } catch(Exception e) {
                //ignore
            }
        }
    }

    @Override
    public void unlock() {
        Object obj = zkClient.readData(lockPath, true);
        if(null == obj || zkNodeVal != (Integer)obj) {
            throw new IllegalMonitorStateException("Lock not owner by you, can not do unlock.");
        }
        
        zkClient.delete(lockPath);
    }
}
