package com.canal.center.zookeeper.listener;

import java.util.HashSet;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.canal.center.cache.TaskCache;
import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.EventType;
import com.datacanal.common.util.CommonUtils;

/**
 * 物理表的listener
 * 监听/canal/center/task/{tablename}这个目录
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午5:03:31
 */
public class PhysicsTableListener implements IZkChildListener {
    
    public static final Logger LOG = LoggerFactory.getLogger(PhysicsTableListener.class);
    
    private ZkClient zkClient;
    
    public PhysicsTableListener(ZkClient zkClientArg) {
        this.zkClient = zkClientArg;
    }
    
    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
        //"逻辑表"添加或者删除数据库表的分片
        //老的分片信息
        HashSet<String> oldAllLogicTables = TaskCache.instance().getLogicToPhysicsPaths().get(parentPath);
        //新的分片信息
        HashSet<String> newAllLogicTables = CommonUtils.convertToFullPath(parentPath, new HashSet<>(currentChilds));
       
        //添加的分片
        HashSet<String> adds = CommonUtils.comparaHashsetToAdd(newAllLogicTables, oldAllLogicTables);
        if(0!=adds.size()) {
            handleAdd(adds, parentPath);
        }
        
        TaskCache.instance().getLogicToPhysicsPaths().put(parentPath, newAllLogicTables);
    }
    
    /**
     * 
     * @param adds
     * @param parentPath
     */
    public void handleAdd(HashSet<String> adds, String parentPath) {
        for (String add : adds) {
            if(zkClient.exists(add)) {
                //instance挂载的目录
                StringBuilder instancePath = new StringBuilder();
                instancePath.append(add).append(Consts.ZK_PATH_SEPARATOR).append("instance");
                
                zkClient.subscribeChildChanges(instancePath.toString(), new InstancListener(zkClient));
                //添加新的分片,需要到其他node上启动instance
                Command command = new Command();
                command.setEventType(EventType.INSTANCE_START);
                command.setObj(add);
                LOG.info("Add command : " + command.toString());
                TaskCache.instance().getCommands().offer(command);
            }
        }
    }
}
