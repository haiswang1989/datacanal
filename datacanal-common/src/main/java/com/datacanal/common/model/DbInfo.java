package com.datacanal.common.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据库的信息
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午6:18:46
 */
@Getter
@Setter
@AllArgsConstructor
public class DbInfo {
    
    private String host; 
    private int port;
    private String username;
    private String password;
    private String dbName;
    private Set<String> sensitiveTables;
    private String zkPath;
}
