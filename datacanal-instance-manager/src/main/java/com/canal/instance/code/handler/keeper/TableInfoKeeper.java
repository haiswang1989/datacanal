package com.canal.instance.code.handler.keeper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.canal.instance.code.mysql.DbMetadata;
import com.datacanal.common.model.ColumnInfo;
import com.datacanal.common.model.TableInfo;
import com.google.code.or.binlog.impl.event.TableMapEvent;

import lombok.Getter;

/**
 * 保存TABLE_MAP_EVENT中提取到的的信息
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月23日 上午11:25:13
 */
public class TableInfoKeeper {
    
    //表的id和表信息的映射
    private static Map<Long, TableInfo> tableIdMap = new ConcurrentHashMap<>();
    
    //表名和表的字段集合的映射
    private static Map<String,List<ColumnInfo>> tableNameToColumns = new ConcurrentHashMap<>();
    
    @Getter
    private static String binlogName;
    
    /**
     * 初始化表结构
     */
    public static void init(String binlogNameArg) {
        tableNameToColumns = DbMetadata.getColumns();
        binlogName = binlogNameArg;
    }
    
    /**
     * 表结构变换,刷新表结构
     */
    public static synchronized void refreshColumnsMap(){
        Map<String,List<ColumnInfo>> map = DbMetadata.getColumns();
        if(map.size() > 0){
            tableNameToColumns = map;
        }
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
     * 表的信息
     * @param tableId
     * @return
     */
    public static TableInfo getTableInfo(long tableId){
        return tableIdMap.get(tableId);
    }
    
    /**
     * 获取表结构
     * @param fullName 表的全名
     * @return
     */
    public static List<ColumnInfo> getColumns(String fullName){
        return tableNameToColumns.get(fullName);
    }
}
