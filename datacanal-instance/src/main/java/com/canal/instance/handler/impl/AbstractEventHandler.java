package com.canal.instance.handler.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.canal.instance.handler.intf.IEventHandler;
import com.canal.instance.handler.keeper.TableInfoKeeper;
import com.datacanal.common.model.ColumnInfo;
import com.google.code.or.common.glossary.Column;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:35:51
 */
public abstract class AbstractEventHandler implements IEventHandler {
    
    /**
     * ROW_EVENT中是没有Column信息的，需要通过MysqlConnection（下面会讲到）的方式读取列名信息，
     * 然后跟取回的List<Column>进行映射。
     * 
     * @param cols
     * @param databaseName
     * @param tableName
     * @return
     */
    protected Map<String,String> getMap(List<Column> cols, String databaseName, String tableName){
        Map<String,String> map = new HashMap<>();
        if(cols == null || cols.size()==0){
            return null;
        }

        String fullName = databaseName+"."+tableName;
        List<ColumnInfo> columnInfoList = TableInfoKeeper.getColumns(fullName);
        if(columnInfoList == null)
            return null;
        if(columnInfoList.size() != cols.size()){
            TableInfoKeeper.refreshColumnsMap();
            if(columnInfoList.size() != cols.size())
            {
                System.out.println("columnInfoList.size is not equal to cols.");
                return null;
            }
        }

        for(int i=0;i<columnInfoList.size(); i++){
            if(cols.get(i).getValue()==null)
                map.put(columnInfoList.get(i).getName(),"");
            else
                map.put(columnInfoList.get(i).getName(), cols.get(i).toString());
        }

        return map;
    }
    
}
