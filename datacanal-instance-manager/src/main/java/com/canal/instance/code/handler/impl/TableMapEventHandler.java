package com.canal.instance.code.handler.impl;

import org.springframework.stereotype.Component;

import com.canal.instance.code.handler.keeper.TableInfoKeeper;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.impl.event.TableMapEvent;

/**
 * 处理TABLE_MAP_EVENT事件
 * 
 * 每次row_event前都伴随着TABLE_MAP_EVENT事件,保存一些表的信息
 * 如tableid,tableName, databaseName, 而ROW_EVENT只有tableId
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:38:05
 */
@Component
public class TableMapEventHandler extends AbstractEventHandler {

    @Override
    public void handle(BinlogEventV4 event) {
        TableMapEvent tableMapEvent = (TableMapEvent)event;
        TableInfoKeeper.saveTableIdMap(tableMapEvent);
    }
}
