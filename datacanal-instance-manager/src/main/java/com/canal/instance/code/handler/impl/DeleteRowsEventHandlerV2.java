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
import com.google.code.or.binlog.impl.event.DeleteRowsEventV2;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Row;

/**
 * 处理DELETE_ROWS_EVENT_V2事件(MYSQL 5.6版本)
 * 对应delete语句 
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:39:44
 */
@Component
public class DeleteRowsEventHandlerV2 extends AbstractEventHandler {
    
    @Resource(name="jsonSerializer")
    private ISerializer<CDCEvent> serializer;
    
    @Resource(name="kafkaSender")
    private ISender sender;
    
    @Override
    public void handle(BinlogEventV4 event) {
        DeleteRowsEventV2 deleteRowsEvent = (DeleteRowsEventV2)event;
        
        long tableId = deleteRowsEvent.getTableId();
        TableInfo tableInfo = TableInfoKeeper.getTableInfo(tableId);
        
        String databaseName = tableInfo.getDatabaseName();
        String tableName = tableInfo.getTableName();
        
        List<Row> rows = deleteRowsEvent.getRows();
        for (Row row : rows) {
            //delete之前row的值
            List<Column> before = row.getColumns();
            Map<String,String> beforeMap = getMap(before,databaseName,tableName);
            
            if(beforeMap !=null && beforeMap.size()>0){
                CDCEvent cdcEvent = new CDCEvent(deleteRowsEvent,databaseName,tableName, TableInfoKeeper.getBinlogName());
                cdcEvent.setIsDdl(false);
                cdcEvent.setSql(null);
                cdcEvent.setBefore(beforeMap);
                sender.sendKafka(tableInfo.getFullName(), serializer.encode(cdcEvent));
            }
        }
    }
}
