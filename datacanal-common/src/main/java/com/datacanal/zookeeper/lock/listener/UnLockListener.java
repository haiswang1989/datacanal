package com.datacanal.zookeeper.lock.listener;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.I0Itec.zkclient.IZkChildListener;

/**
 * 解锁的listener 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月31日 下午6:37:57
 */
public class UnLockListener implements IZkChildListener {
    
    private CountDownLatch latch;
    
    public UnLockListener(CountDownLatch latchArg) {
        this.latch = latchArg;
    }
    
    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
        if(null==currentChilds || 0==currentChilds.size()) {
            //解锁
            latch.countDown();
        }
    }
}
