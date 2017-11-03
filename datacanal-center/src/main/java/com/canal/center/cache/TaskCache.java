package com.canal.center.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.I0Itec.zkclient.ZkClient;
import com.canal.center.zookeeper.listener.InstancListener;
import com.canal.center.zookeeper.listener.LogicTableListener;
import com.canal.center.zookeeper.listener.PhysicsTableListener;
import com.datacanal.common.constant.Consts;

/**
 * 任务缓存
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午5:16:04
 */
public class TaskCache {
    
    //逻辑表的path
    private HashSet<String> logicPaths;
    
    //物理表的path
    private HashMap<String, HashSet<String>> logicToPhysicsPaths;
    
    //物理表和运行的instance的比对
    private HashMap<String, String> physicsPathToInstance;
    
    private static class SingletonHolder {
        private static final TaskCache taskCache = new TaskCache();
    }
    
    public static TaskCache instance() {
        return SingletonHolder.taskCache;
    }
    
    private TaskCache() {
        logicPaths = new HashSet<>();
        logicToPhysicsPaths = new HashMap<>();
        physicsPathToInstance = new HashMap<>();
    }
    
    /**
     * 重新load任务,并进行监控
     * @param zkClient
     */
    public void loadTask(ZkClient zkClient) {
        List<String> tmpLogicPaths = zkClient.getChildren(Consts.ZK_PATH_TASK);
        //对逻辑表的变化进行监控(新的表监控上线)
        zkClient.subscribeChildChanges(Consts.ZK_PATH_TASK, new LogicTableListener());
        logicPaths.clear();
        for (String tmpLogicPath : tmpLogicPaths) {
            String fullLogicPath = Consts.ZK_PATH_TASK + Consts.ZK_PATH_SEPARATOR + tmpLogicPath;
            //所有的逻辑表
            logicPaths.add(fullLogicPath);
            //物理表
            List<String> tmpPhysicsPaths = zkClient.getChildren(fullLogicPath);
            //对物理表变化进行监控(上线或者下线分片)
            zkClient.subscribeChildChanges(fullLogicPath, new PhysicsTableListener());
            HashSet<String> physics = new HashSet<>();
            for (String tmpPhysicsPath : tmpPhysicsPaths) {
                String fullPhysicsPath = fullLogicPath + Consts.ZK_PATH_SEPARATOR + tmpPhysicsPath;
                physics.add(fullPhysicsPath);
                List<String> instances = zkClient.getChildren(fullPhysicsPath);
                //对物理表的运行instance进行监控(instance挂掉以后触发)
                zkClient.subscribeChildChanges(fullPhysicsPath, new InstancListener(zkClient));
                if(null==instances || 0==instances.size()) {
                    physicsPathToInstance.put(fullPhysicsPath, null);
                } else {
                    String instance = instances.get(0);
                    physicsPathToInstance.put(fullPhysicsPath, instance);
                }
            }
            
            //逻辑表和物理表的映射
            logicToPhysicsPaths.put(fullLogicPath, physics);
        }
    }
}
