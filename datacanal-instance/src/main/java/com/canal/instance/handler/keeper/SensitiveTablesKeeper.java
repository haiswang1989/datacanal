package com.canal.instance.handler.keeper;

import java.util.Set;

import lombok.Setter;

/**
 * 敏感表：需要对该类表的变化做处理
 * 非敏感表：这类表的Event可以直接忽略
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月10日 上午10:09:33
 */
public class SensitiveTablesKeeper {
    
    @Setter
    private static Set<String> sensitiveTables;
    
    /**
     * 是否是敏感表
     * @param tablename
     * @return
     */
    public static boolean isSensitiveTable(String tablename) {
        return sensitiveTables.contains(tablename);
    }
}
