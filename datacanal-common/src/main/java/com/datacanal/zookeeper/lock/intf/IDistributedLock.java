package com.datacanal.zookeeper.lock.intf;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁的接口
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月31日 下午5:30:46
 */
public interface IDistributedLock {
    
    public void lock();
    
    public void unlock();
    
    public void lockInterruptibly() throws InterruptedException;
    
    public boolean tryLock(); 
    
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException; 
}
