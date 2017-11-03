package com.datacanal.zookeeper.lock.impl;

import java.util.concurrent.TimeUnit;
import com.datacanal.zookeeper.lock.intf.IDistributedLock;

/**
 * 分布式锁的默认实现
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月31日 下午5:34:40
 */
public abstract class AbstractDistributedLock implements IDistributedLock {
    
    @Override
    public void lock() {
        throw new UnsupportedOperationException("Unsupported method.");
    }
    
    @Override
    public void unlock() {
        throw new UnsupportedOperationException("Unsupported method.");
    }
    
    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException("Unsupported method.");
    }
    
    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException("Unsupported method.");
    }
    
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Unsupported method.");
    }
}
