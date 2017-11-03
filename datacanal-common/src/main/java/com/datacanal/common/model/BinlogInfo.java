package com.datacanal.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * binlog文件的信息
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月23日 下午12:05:42
 */
@Data
@AllArgsConstructor
public class BinlogInfo {
    
    /**
     * mysql binlog的文件名称
     */
    private String binlogName;
    
    /**
     * binlog的字节数
     */
    private Long fileSize;
}
