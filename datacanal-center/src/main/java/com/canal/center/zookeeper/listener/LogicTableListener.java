package com.canal.center.zookeeper.listener;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;

/**
 * 逻辑表的listener
 * 监听/canal/center/task这个目录
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午5:02:12
 */
public class LogicTableListener implements IZkChildListener {

    @Override
    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
        
    }
}
