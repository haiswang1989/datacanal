package com.datacanal.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表的column信息
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月23日 上午11:53:51
 */
@Data
@AllArgsConstructor
public class ColumnInfo {
    
    /**
     * 字段名称
     */
    private String name;
    
    /**
     * 字段类型
     */
    private String type;
}
