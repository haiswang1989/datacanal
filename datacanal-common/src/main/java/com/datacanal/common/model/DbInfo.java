package com.datacanal.common.model;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
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
    
    
    
    //主节点
    private DbNode master;
    //所有的从节点
    private List<DbNode> slaves;
    //抽取的表
    private Set<String> sensitiveTables;
    //任务路径
    private String zkPath;
}

@Data
class DbNode {
    private String host;
    private int port;
    private String username;
    private String password;
    private String dbName;
} 
