package com.canal.instance.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.canal.instance.handler.EventHandler;
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
    
    @Autowired
    private EventHandler eventHandler;
    
    @Override
    public void onEvents(BinlogEventV4 event) {
        if(null==event) {
            LOG.warn("Event is null.");
            return;
        }
        
        int eventType = event.getHeader().getEventType();
        eventHandler.enhanceHandler(event, eventType);
    }
}
