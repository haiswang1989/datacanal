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
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Pair;
import com.google.code.or.common.glossary.Row;

/**
 * 处理UPDATE_ROWS_EVENT事件
 * 对应update语句
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:41:10
 */
@Component
public class UpdateRowsEventHandler extends AbstractEventHandler {
    
    @Resource(name="avroSerializer")
    private ISerializer<CDCEvent> serializer;
    
    @Resource(name="kafkaSender")
    private ISender sender;
    
    @Override
    public void handle(BinlogEventV4 event) {
        UpdateRowsEvent updateRowsEvent = (UpdateRowsEvent)event;
        
        long tableId = updateRowsEvent.getTableId();
        TableInfo tableInfo = TableInfoKeeper.getTableInfo(tableId);
        
        String databaseName = tableInfo.getDatabaseName();
        String tableName = tableInfo.getTableName();
        
        List<Pair<Row>> rows = updateRowsEvent.getRows();
        for (Pair<Row> pair : rows) {
            List<Column> colsBefore = pair.getBefore().getColumns();
            List<Column> colsAfter = pair.getAfter().getColumns();
            
            Map<String,String> beforeMap = getMap(colsBefore,databaseName,tableName);
            Map<String,String> afterMap = getMap(colsAfter,databaseName,tableName);
            
            if(beforeMap!=null && afterMap!=null && beforeMap.size()>0 && afterMap.size()>0) {
                CDCEvent cdcEvent = new CDCEvent(updateRowsEvent,databaseName,tableName);
                cdcEvent.setIsDdl(false);
                cdcEvent.setSql(null);
                cdcEvent.setBefore(beforeMap);
                cdcEvent.setAfter(afterMap);
                sender.send(tableInfo.getFullName(), serializer.encode(cdcEvent));
            }
        }
    }
}
