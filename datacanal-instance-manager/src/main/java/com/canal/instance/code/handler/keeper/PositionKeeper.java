package com.canal.instance.code.handler.keeper;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;

import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.Status;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月9日 下午6:04:00
 */
public class PositionKeeper {
    
    @Setter
    @Getter
    private static volatile long position = 0l;
    
    @Setter
    private static int positionSyncZkPeriod;
    
    @Setter
    private static ZkClient zkClient;
    
    @Setter
    @Getter
    private static Status status = Status.RUNNING;
    
    public static void init(String zkPath) {
        position = getLastPositionFromZk(zkPath);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new SyncZookeeper(zkPath), 0, positionSyncZkPeriod, TimeUnit.SECONDS);
    }
    
    /**
     * 上一次停止时的zk中记录的position
     * @param path
     */
    private static long getLastPositionFromZk(String path) {
        StringBuilder positionPath = new StringBuilder();
        positionPath.append(path).append(Consts.ZK_PATH_SEPARATOR).append(Consts.DATACANAL_TASK_POSITION);
        return zkClient.readData(positionPath.toString());
    }
    
    /**
     * 同步zookeeper的线程
     * <p>Description:</p>
     * @author hansen.wang
     * @date 2017年11月14日 下午3:52:43
     */
    static class SyncZookeeper implements Runnable {
        
        private String fullZkPath;
        private long lastPosition = 0l; 
        
        public SyncZookeeper(String zkPathArg) {
            this.fullZkPath = zkPathArg + Consts.ZK_PATH_SEPARATOR + Consts.DATACANAL_TASK_POSITION;
        }
        
        @Override
        public void run() {
            if(lastPosition != position) {
                //将position同步到zk
                zkClient.writeData(this.fullZkPath, position);
                lastPosition = position;
            }
        }
    }
}
