package com.datacanal.common.util;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        DbInfo dbInfo = new DbInfo("192.168.56.101", 3306, "root", "123456", "test");
        String path = "/canal/center/task/vip_person/person";
        zkClient.create(path, dbInfo.toString(), CreateMode.PERSISTENT);
    }
    
}
