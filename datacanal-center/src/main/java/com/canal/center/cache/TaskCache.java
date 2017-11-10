package com.canal.center.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.canal.center.CanalCenterLauncher;
import com.canal.center.zookeeper.listener.InstancListener;
import com.canal.center.zookeeper.listener.LogicTableListener;
import com.canal.center.zookeeper.listener.PhysicsTableListener;
import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.EventType;

import lombok.Getter;
import lombok.Setter;

/**
 * 任务缓存
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午5:16:04
 */
public class TaskCache {
    
    public static final Logger LOG = LoggerFactory.getLogger(CanalCenterLauncher.class);
    
    //逻辑表的path
    @Getter
    @Setter
    private HashSet<String> logicPaths;
    
    //物理表的path
    @Getter
    @Setter
    private HashMap<String, HashSet<String>> logicToPhysicsPaths;
    
    //物理表和运行的instance的比对
    @Getter
    @Setter
    private HashMap<String, String> physicsPathToInstance;
    
    @Getter
    private LinkedBlockingQueue<Command> commands;
    
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
        commands = new LinkedBlockingQueue<>();
    }
    
    /**
     * 重新load任务,并进行监控
     * @param zkClient
     */
    public void loadTask(ZkClient zkClient) {
        List<String> tmpLogicPaths = zkClient.getChildren(Consts.DATACANAL_TASK);
        //对逻辑表的变化进行监控(新的表监控上线)
        zkClient.subscribeChildChanges(Consts.DATACANAL_TASK, new LogicTableListener(zkClient));
        logicPaths.clear();
        for (String tmpLogicPath : tmpLogicPaths) {
            String fullLogicPath = Consts.DATACANAL_TASK + Consts.ZK_PATH_SEPARATOR + tmpLogicPath;
            //所有的逻辑表
            logicPaths.add(fullLogicPath);
            //物理表
            List<String> tmpPhysicsPaths = zkClient.getChildren(fullLogicPath);
            //对物理表变化进行监控(上线或者下线分片)
            zkClient.subscribeChildChanges(fullLogicPath, new PhysicsTableListener(zkClient));
            HashSet<String> physics = new HashSet<>();
            for (String tmpPhysicsPath : tmpPhysicsPaths) {
                String fullPhysicsPath = fullLogicPath + Consts.ZK_PATH_SEPARATOR + tmpPhysicsPath;
                physics.add(fullPhysicsPath);
                
                //instance的路径
                String instancePath =  fullPhysicsPath + Consts.ZK_PATH_SEPARATOR + Consts.DATACANAL_TASK_INSTANCE;
                //对物理表的运行instance进行监控(instance挂掉以后触发)
                zkClient.subscribeChildChanges(instancePath, new InstancListener(zkClient));
                
                List<String> runningInstance = zkClient.getChildren(instancePath);
                
                if(null==runningInstance || 0==runningInstance.size()) {
                    //这边需要发送一个启动instance的命令
                    physicsPathToInstance.put(fullPhysicsPath, null);
                    //添加新的分片,需要到其他node上启动instance
                    Command command = new Command();
                    command.setEventType(EventType.INSTANCE_START);
                    command.setObj(fullPhysicsPath);
                    LOG.info("Add command : " + command.toString());
                    TaskCache.instance().getCommands().offer(command);
                    
                } else {
                    String val1 = runningInstance.get(0);
                    String val2 = runningInstance.get(1);
                    
                    String ip = null;
                    if(-1!=val1.indexOf(".")) {
                        ip = val1;
                    } else {
                        ip = val2;
                    }
                    
                    String instance = instancePath + Consts.ZK_PATH_SEPARATOR + ip;
                    physicsPathToInstance.put(fullPhysicsPath, instance);
                }
            }
            
            //逻辑表和物理表的映射
            logicToPhysicsPaths.put(fullLogicPath, physics);
        }
    }
}
