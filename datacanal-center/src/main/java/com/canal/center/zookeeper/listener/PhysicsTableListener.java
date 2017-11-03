package com.canal.center.zookeeper.listener;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;

/**
 * 物理表的listener
 * 监听/canal/center/task/{tablename}这个目录
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午5:03:31
 */
public class PhysicsTableListener implements IZkChildListener {

    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
        // TODO Auto-generated method stub
        
    }

}
