package com.datacanal.common.util;

import java.util.HashSet;
import java.util.Set;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSON;
import com.datacanal.common.model.DbInfo;
import com.datacanal.common.model.DbNode;
import com.datacanal.common.model.Status;

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
        DbNode dbNode = new DbNode("192.168.56.101", 3306, "root", "123456", "test");
        DbInfo dbInfo = new DbInfo(dbNode, null, sensitiveTables, "/datacanal/task/person/person-1", true);
        String path = "/datacanal/task/person/person-1";
        zkClient.create(path, JSON.toJSONString(dbInfo), CreateMode.PERSISTENT);
        
        zkClient.create(path + "/instance", "", CreateMode.PERSISTENT);
        zkClient.create(path + "/position", 0L, CreateMode.PERSISTENT);
    }
    
    @Test
    public void test() {
        String ip = "192.168.0.1";
        System.out.println(ip.indexOf("."));
        
        String parentPath = "/1/2/3/instance";
        String tmp = parentPath.substring(0, parentPath.lastIndexOf("/instance"));
        System.out.println(tmp);
    }
    
    @Test
    public void testExecCmd() {
        CommonUtils.doExecCmd("dir");
    }
    
    @Test
    public void testModifyData() {
        zkClient.writeData("/datacanal/task/person/person-1/instance/10000", Status.STOP);
        System.out.println("Over...");
    }
}
