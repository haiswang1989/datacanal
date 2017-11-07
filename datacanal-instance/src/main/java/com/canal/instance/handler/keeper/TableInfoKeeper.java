package com.canal.instance.handler.keeper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.datacanal.common.model.ColumnInfo;
import com.datacanal.common.model.DbInfo;
import com.datacanal.common.model.TableInfo;
import com.google.code.or.binlog.impl.event.TableMapEvent;

/**
 * 保存TABLE_MAP_EVENT中提取到的的信息
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月23日 上午11:25:13
 */
public class TableInfoKeeper {
    
    /**
     * 表的id和表信息的映射
     */
    private static Map<Long, TableInfo> tableIdMap = new ConcurrentHashMap<>();
    
    /**
     * 表名和表的字段集合的映射
     */
    private static Map<String,List<ColumnInfo>> columnsMap = new ConcurrentHashMap<>();
    
    static {
//        columnsMap = DbInfo.getColumns();
        //TODO
    }
    
    /**
     * 
     * @param tableMapEvent
     */
    public static void saveTableIdMap(TableMapEvent tableMapEvent) {
        long tableId = tableMapEvent.getTableId();
        tableIdMap.remove(tableId);
        
        TableInfo tableInfo = new TableInfo();
        tableInfo.setDatabaseName(tableMapEvent.getDatabaseName().toString());
        tableInfo.setTableName(tableMapEvent.getTableName().toString());
        tableInfo.setFullName(tableMapEvent.getDatabaseName() + "." + tableMapEvent.getTableName());
        tableIdMap.put(tableId, tableInfo);
    }
    
    /**
     * 
     */
    public static synchronized void refreshColumnsMap(){
        //TODO
//        Map<String,List<ColumnInfo>> map = DbInfo.getColumns();
//        if(map.size()>0){
//            columnsMap = map;
//        }
    }
    
    /**
     * 
     * @param tableId
     * @return
     */
    public static TableInfo getTableInfo(long tableId){
        return tableIdMap.get(tableId);
    }
    
    /**
     * 
     * @param fullName
     * @return
     */
    public static List<ColumnInfo> getColumns(String fullName){
        return columnsMap.get(fullName);
    }
}
