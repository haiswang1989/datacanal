package com.canal.instance.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.canal.instance.handler.EventHandler;
import com.google.code.or.binlog.BinlogEventV4;

/**
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午2:16:51
 */
@Component
public class CDCInstanceListener extends AbstractInstanceListener {
    
    @Autowired
    private EventHandler eventHandler;
    
    @Override
    public void onEvents(BinlogEventV4 event) {
        if(null==event) {
            System.out.println("Event is null.");
            return;
        }
        
        int eventType = event.getHeader().getEventType();
        eventHandler.enhanceHandler(event, eventType);
    }
}
