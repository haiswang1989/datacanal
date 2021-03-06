package com.canal.instance.code.handler;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.canal.instance.code.handler.impl.AbstractEventHandler;
import com.canal.instance.code.handler.impl.DeleteRowsEventHandler;
import com.canal.instance.code.handler.impl.DeleteRowsEventHandlerV2;
import com.canal.instance.code.handler.impl.EmptyEventHandler;
import com.canal.instance.code.handler.impl.TableMapEventHandler;
import com.canal.instance.code.handler.impl.UpdateRowsEventHandler;
import com.canal.instance.code.handler.impl.UpdateRowsEventHandlerV2;
import com.canal.instance.code.handler.impl.WriteRowsEventHandler;
import com.canal.instance.code.handler.impl.WriteRowsEventHandlerV2;
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
    private DeleteRowsEventHandlerV2 deleteRowsEventHandlerV2;
    
    @Autowired
    private TableMapEventHandler tableMapEventHandler;
    
    @Autowired
    private UpdateRowsEventHandler updateRowsEventHandler;
    
    @Autowired
    private UpdateRowsEventHandlerV2 updateRowsEventHandlerV2;
    
    @Autowired
    private WriteRowsEventHandler writeRowsEventHandler;
    
    @Autowired
    private WriteRowsEventHandlerV2 writeRowsEventHandlerV2;
    
    @Autowired
    private EmptyEventHandler emptyEventHandler;
    
    private AbstractEventHandler[] eventHandlers = new AbstractEventHandler[128];
    
    @PostConstruct
    public void init() {
        //这部分是针对mysql5.6以前的版本
        eventHandlers[MySQLConstants.WRITE_ROWS_EVENT] = writeRowsEventHandler;
        eventHandlers[MySQLConstants.DELETE_ROWS_EVENT] = deleteRowsEventHandler;
        eventHandlers[MySQLConstants.UPDATE_ROWS_EVENT] = updateRowsEventHandler;
        
        eventHandlers[MySQLConstants.TABLE_MAP_EVENT] = tableMapEventHandler;
        //这部分是针对mysql5.6的版本
        eventHandlers[MySQLConstants.DELETE_ROWS_EVENT_V2] = deleteRowsEventHandlerV2;
        eventHandlers[MySQLConstants.UPDATE_ROWS_EVENT_V2] = updateRowsEventHandlerV2;
        eventHandlers[MySQLConstants.WRITE_ROWS_EVENT_V2] = writeRowsEventHandlerV2;
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
