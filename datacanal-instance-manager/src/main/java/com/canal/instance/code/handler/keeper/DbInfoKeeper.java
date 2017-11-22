package com.canal.instance.code.handler.keeper;

import java.util.Set;

import com.datacanal.common.model.DbInfo;

/**
 * Task的Db信息
 *  
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月10日 上午10:09:33
 */
public class DbInfoKeeper {
    
    private static Set<String> sensitiveTables;
    
    public static void init(DbInfo dbInfo) {
        sensitiveTables = dbInfo.getSensitiveTables();
    }
    
        
    /**
     * 是否是敏感表
     * @param tablename
     * @return
     */
    public static boolean isSensitiveTable(String tablename) {
        if(null==sensitiveTables) {
            return true;
        }
        
        return sensitiveTables.contains(tablename);
    }
}
