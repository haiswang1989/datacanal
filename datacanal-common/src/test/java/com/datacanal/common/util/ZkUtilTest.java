package com.datacanal.common.util;

import java.util.HashSet;
import java.util.Set;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSON;
import com.datacanal.common.model.DbInfo;

public class ZkUtilTest {
    
    ZkClient zkClient;
    
    @BeforeClass
    public void setupENV() {
        String zkString = "10.199.188.79:2181,10.199.187.101:2181,10.199.187.102:2181";
        zkClient = new ZkClient(zkString);
    }
    
    /**
     * 
     */
    @Test
    public void testCreatePath() {
        Set<String> sensitiveTables = new HashSet<>();
        sensitiveTables.add("person");
        DbInfo dbInfo = new DbInfo("192.168.56.101", 3306, "root", "123456", "test", sensitiveTables);
        String path = "/datacanal/task/person/person-1";
        zkClient.create(path, JSON.toJSONString(dbInfo), CreateMode.PERSISTENT);
    }
    
}
