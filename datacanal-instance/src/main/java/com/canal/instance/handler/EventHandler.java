package com.canal.instance.handler;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.canal.instance.handler.impl.AbstractEventHandler;
import com.canal.instance.handler.impl.DeleteRowsEventHandler;
import com.canal.instance.handler.impl.EmptyEventHandler;
import com.canal.instance.handler.impl.TableMapEventHandler;
import com.canal.instance.handler.impl.UpdateRowsEventHandler;
import com.canal.instance.handler.impl.WriteRowsEventHandler;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.common.util.MySQLConstants;

/**
 * 统一的事件处类
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午12:04:05
 */
@Component
public class EventHandler extends AbstractEventHandler {
    
    @Autowired
    private DeleteRowsEventHandler deleteRowsEventHandler;
    
    @Autowired
    private TableMapEventHandler tableMapEventHandler;
    
    @Autowired
    private UpdateRowsEventHandler updateRowsEventHandler;
    
    @Autowired
    private WriteRowsEventHandler writeRowsEventHandler;
    
    @Autowired
    private EmptyEventHandler emptyEventHandler;
    
    private AbstractEventHandler[] eventHandlers = new AbstractEventHandler[128];
    
    @PostConstruct
    public void init() {
        eventHandlers[MySQLConstants.DELETE_ROWS_EVENT] = deleteRowsEventHandler;
        eventHandlers[MySQLConstants.TABLE_MAP_EVENT] = tableMapEventHandler;
        eventHandlers[MySQLConstants.UPDATE_ROWS_EVENT] = updateRowsEventHandler;
        eventHandlers[MySQLConstants.WRITE_ROWS_EVENT] = writeRowsEventHandler;
    }
    
    @Override
    public void handle(BinlogEventV4 event) {
    }
    
    /**
     * 
     * @param event
     * @param eventType
     * @return
     */
    public void enhanceHandler(BinlogEventV4 event, int eventType) {
        AbstractEventHandler eventHandler = eventHandlers[eventType];
        if(null==eventHandler) {
            eventHandler = emptyEventHandler;
        }
        
        eventHandler.handle(event);
    }
}
