package com.datacanal.common.model;

import lombok.Data;

/**
 * 当前正在写的binlog的信息 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月23日 下午12:10:49
 */
@Data
public class BinlogMasterStatus {
    
    /**
     * binlog的名称
     */
    private String binlogName;
    
    /**
     * 已经读取到的位置
     */
    private long position;
}
