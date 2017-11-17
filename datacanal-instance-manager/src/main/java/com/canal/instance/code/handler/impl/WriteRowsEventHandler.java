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
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Row;

/**
 * 处理WRITE_ROWS_EVENT事件
 * 对应 insert语句
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:42:28
 */
@Component
public class WriteRowsEventHandler extends AbstractEventHandler {
    
    @Resource(name="jsonSerializer")
    private ISerializer<CDCEvent> serializer;
    
    @Resource(name="kafkaSender")
    private ISender sender;
    
    @Override
    public void handle(BinlogEventV4 event) {
        WriteRowsEvent writeRowsEvent = (WriteRowsEvent)event;
        long tableId = writeRowsEvent.getTableId();
        
        TableInfo tableInfo  =TableInfoKeeper.getTableInfo(tableId);
        String databaseName = tableInfo.getDatabaseName();
        String tableName = tableInfo.getTableName();
        
        List<Row> rows = writeRowsEvent.getRows();
        for (Row row : rows) {
            List<Column> after = row.getColumns();
            Map<String,String> afterMap = getMap(after,databaseName,tableName);
            if(afterMap!=null && afterMap.size()>0){
                CDCEvent cdcEvent = new CDCEvent(writeRowsEvent,databaseName,tableName, TableInfoKeeper.getBinlogName());
                cdcEvent.setIsDdl(false);
                cdcEvent.setSql(null);
                cdcEvent.setAfter(afterMap);
                sender.sendKafka(tableInfo.getFullName(), serializer.encode(cdcEvent));
            }
        }
    }
}
