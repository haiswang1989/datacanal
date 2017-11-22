package com.canal.instance.code.handler.keeper;

import java.util.List;
import java.util.Set;

import com.canal.instance.code.CDCEngine;
import com.datacanal.common.model.DbInfo;
import com.datacanal.common.model.DbNode;

import lombok.Getter;
import lombok.Setter;

/**
 * Task的Db信息
 *  
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月10日 上午10:09:33
 */
public class CommonKeeper {
    
    private static Set<String> sensitiveTables;
    
    private static boolean useMaster;
    
    @Getter
    private static List<DbNode> slaves;
    
    @Getter
    @Setter
    private static DbNode currentSlave;
    
    @Setter
    @Getter
    private static CDCEngine engine;
    
    public static void init(DbInfo dbInfo) {
        sensitiveTables = dbInfo.getSensitiveTables();
        useMaster = dbInfo.isUseMaster();
        slaves = dbInfo.getSlaves();
    }
    
    /**
     * 是否抽取的master
     * @return
     */
    public static boolean isExtractMaster() {
        return useMaster;
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
