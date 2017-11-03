package com.datacanal.common.model;

import com.alibaba.fastjson.JSON;

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
    
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
