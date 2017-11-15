package com.canal.instance.code.zklistener;

import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.canal.instance.code.handler.keeper.PositionKeeper;
import com.datacanal.common.model.Status;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月14日 下午4:32:39
 */
public class StatusListener implements IZkDataListener {
    
    public static final Logger LOG = LoggerFactory.getLogger(StatusListener.class);
    
    private int stopAfterGetCmd;
    
    public StatusListener(int stopAfterGetCmdArg) {
        this.stopAfterGetCmd = stopAfterGetCmdArg;
    }
    
    @Override
    public void handleDataChange(String dataPath, Object data) throws Exception {
        Status newStatus = (Status)data;
        if(newStatus == Status.STOP) {
            LOG.info("Get stop myself command ,prepare to stop.");
            PositionKeeper.setStatus(newStatus);
            //等待position同步到zk
            TimeUnit.SECONDS.sleep(stopAfterGetCmd * 2);
            LOG.info("Stop myself.");
            //终止虚拟机
            System.exit(0);
        }
    }

    @Override
    public void handleDataDeleted(String dataPath) throws Exception {
        
    }
}
