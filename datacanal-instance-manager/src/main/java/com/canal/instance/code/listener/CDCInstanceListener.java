package com.canal.instance.code.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.canal.instance.code.handler.EventHandler;
import com.canal.instance.code.handler.keeper.PositionKeeper;
import com.canal.instance.code.handler.keeper.TableInfoKeeper;
import com.datacanal.common.model.Status;
import com.google.code.or.binlog.BinlogEventV4;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午2:16:51
 */
@Component
public class CDCInstanceListener extends AbstractInstanceListener {
    
    public static final Logger LOG = LoggerFactory.getLogger(CDCInstanceListener.class);
    
    //BinlogEventV4事件处理器
    @Autowired
    private EventHandler eventHandler;
    
    @Override
    public void onEvents(BinlogEventV4 event) {
        if(null==event) {
            LOG.warn("Event is null.");
            return;
        }
        
        if(PositionKeeper.getStatus() == Status.STOP) {
            LOG.info("Need to stop.");
            return;
        }
        
        int eventType = event.getHeader().getEventType();
        eventHandler.enhanceHandler(event, eventType);
        
        //更新position
        PositionKeeper.setPosition(event.getHeader().getPosition());
        PositionKeeper.setBinlogName(TableInfoKeeper.getBinlogName());
    }
}
