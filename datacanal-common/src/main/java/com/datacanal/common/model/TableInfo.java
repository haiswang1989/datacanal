package com.datacanal.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表的信息
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月25日 下午5:46:41
 */
@Data
@EqualsAndHashCode
public class TableInfo {
    
    /**
     * DB的名称
     */
    private String databaseName;
    
    /**
     * 表名称
     */
    private String tableName;
    
    /**
     * 表的全名
     * ${databaseName}.${tableName}
     */
    private String fullName;
}
