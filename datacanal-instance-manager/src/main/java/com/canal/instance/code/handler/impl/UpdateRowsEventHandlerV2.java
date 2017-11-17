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
import com.google.code.or.binlog.impl.event.UpdateRowsEventV2;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Pair;
import com.google.code.or.common.glossary.Row;

/**
 * 处理UPDATE_ROWS_EVENT_V2事件(MYSQL 5.6版本)
 * 对应update语句
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:41:10
 */
@Component
public class UpdateRowsEventHandlerV2 extends AbstractEventHandler {
    
    @Resource(name="jsonSerializer")
    private ISerializer<CDCEvent> serializer;
    
    @Resource(name="kafkaSender")
    private ISender sender;
    
    @Override
    public void handle(BinlogEventV4 event) {
        UpdateRowsEventV2 updateRowsEvent = (UpdateRowsEventV2)event;
        
        long tableId = updateRowsEvent.getTableId();
        TableInfo tableInfo = TableInfoKeeper.getTableInfo(tableId);
        
        String databaseName = tableInfo.getDatabaseName();
        String tableName = tableInfo.getTableName();
        
        List<Pair<Row>> rows = updateRowsEvent.getRows();
        for (Pair<Row> pair : rows) {
            //update以前的数据
            List<Column> colsBefore = pair.getBefore().getColumns();
            //update以后的数据
            List<Column> colsAfter = pair.getAfter().getColumns();
            
            Map<String,String> beforeMap = getMap(colsBefore,databaseName,tableName);
            Map<String,String> afterMap = getMap(colsAfter,databaseName,tableName);
            
            if(beforeMap!=null && afterMap!=null && beforeMap.size()>0 && afterMap.size()>0) {
                CDCEvent cdcEvent = new CDCEvent(updateRowsEvent,databaseName,tableName, TableInfoKeeper.getBinlogName());
                cdcEvent.setIsDdl(false);
                cdcEvent.setSql(null);
                cdcEvent.setBefore(beforeMap);
                cdcEvent.setAfter(afterMap);
                sender.sendKafka(tableInfo.getFullName(), serializer.encode(cdcEvent));
            }
        }
    }
}
