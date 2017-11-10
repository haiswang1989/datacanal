package com.canal.center.zookeeper.listener;

import java.util.HashSet;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import com.canal.center.cache.TaskCache;
import com.datacanal.common.util.CommonUtils;

/**
 * 逻辑表的listener
 * 监听/canal/center/task这个目录
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午5:02:12
 */
public class LogicTableListener implements IZkChildListener {
    
    private ZkClient zkClient;
    
    public LogicTableListener(ZkClient zkClientArg) {
        this.zkClient = zkClientArg;
    }

    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
        //添加或删除逻辑表
        HashSet<String> oldAllLogicTables = TaskCache.instance().getLogicPaths();
        HashSet<String> newAllLogicTables = CommonUtils.convertToFullPath(parentPath, new HashSet<>(currentChilds));
        
        //给新加的逻辑表添加监控
        HashSet<String> adds = CommonUtils.comparaHashsetToAdd(newAllLogicTables, oldAllLogicTables);
        if(0!=adds.size()) {
            handleAdds(adds, parentPath);
        }
        
        //对于删除的逻辑表无需做其他动作,只需要缓存中清除
        TaskCache.instance().setLogicPaths(newAllLogicTables);
    }
    
    /**
     * 新的表添加监控
     * @param adds
     * @param parentPath
     */
    public void handleAdds(HashSet<String> adds, String parentPath) {
        for (String add : adds) {
            if(zkClient.exists(add)) {
                zkClient.subscribeChildChanges(add, new PhysicsTableListener(zkClient));
            }
        }
    }
}
