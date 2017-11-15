package com.canal.instance.code.handler.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.canal.instance.code.handler.keeper.TableInfoKeeper;
import com.canal.serializer.intf.ISerializer;
import com.datacanal.common.model.CDCEvent;
import com.datacanal.common.model.TableInfo;
import com.datacanal.sink.intf.ISender;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Row;

/**
 * 处理DELETE_ROWS_EVENT事件
 * 对应delete语句 
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:39:44
 */
@Component
public class DeleteRowsEventHandler extends AbstractEventHandler {
    
    @Resource(name="jsonSerializer")
    private ISerializer<CDCEvent> serializer;
    
    @Resource(name="kafkaSender")
    private ISender sender;
    
    @Override
    public void handle(BinlogEventV4 event) {
        DeleteRowsEvent deleteRowsEvent = (DeleteRowsEvent)event;
        
        long tableId = deleteRowsEvent.getTableId();
        TableInfo tableInfo = TableInfoKeeper.getTableInfo(tableId);
        
        String databaseName = tableInfo.getDatabaseName();
        String tableName = tableInfo.getTableName();
        
        List<Row> rows = deleteRowsEvent.getRows();
        for (Row row : rows) {
            List<Column> before = row.getColumns();
            Map<String,String> beforeMap = getMap(before,databaseName,tableName);
            
            if(beforeMap !=null && beforeMap.size()>0){
                CDCEvent cdcEvent = new CDCEvent(deleteRowsEvent,databaseName,tableName);
                cdcEvent.setIsDdl(false);
                cdcEvent.setSql(null);
                cdcEvent.setBefore(beforeMap);
                sender.sendKafka(tableInfo.getFullName(), serializer.encode(cdcEvent));
            }
        }
    }
}
