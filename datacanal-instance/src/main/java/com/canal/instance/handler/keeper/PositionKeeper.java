package com.canal.instance.handler.keeper;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;

import lombok.Setter;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月9日 下午6:04:00
 */
public class PositionKeeper {
    
    @Setter
    private static volatile long position = 0l;
    
    @Setter
    private static int positionSyncZkPeriod;
    
    @Setter
    private static ZkClient zkClient;
    
    public static void init() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new SyncZookeeper(), 0, positionSyncZkPeriod, TimeUnit.SECONDS);
    }
    
    static class SyncZookeeper implements Runnable {
        @Override
        public void run() {
            long newPosition = position;
            System.out.println(newPosition);
        }
    }
}
