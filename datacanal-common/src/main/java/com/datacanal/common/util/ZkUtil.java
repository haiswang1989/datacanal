package com.datacanal.common.util;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;

import com.datacanal.common.constant.Consts;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午4:11:06
 */
public class ZkUtil {
    
    /**
     * 在父目录下创建子目录
     * @param parentPath
     * @param childPath
     * @param value
     */
    public static void createChildPath(ZkClient zkClient, String parentPath, String childPath, String value, CreateMode mode) {
        StringBuilder createPath = new StringBuilder();
        createPath.append(parentPath).append(Consts.DATACANAL_CANAL_SERVER).append(childPath);
        zkClient.create(createPath.toString(), value, mode);
    }
    
    /**
     * 递归创建目录(如果父目录不存在则创建)
     * @param zkClient
     * @param path
     */
    public static void createPathRecursive(ZkClient zkClient, String path) {
        if(path.endsWith(Consts.ZK_PATH_SEPARATOR)) {
            path = path.substring(0, path.length()-1);
        }
        
        int index = path.lastIndexOf(Consts.ZK_PATH_SEPARATOR);
        if(0 != index) {
            String parentPath = path.substring(0, index);
            if(!zkClient.exists(parentPath)) {
                createPathRecursive(zkClient, parentPath);
            } else {
                zkClient.create(path, "", CreateMode.PERSISTENT);
            } 
        } else {
            zkClient.create(path, "", CreateMode.PERSISTENT);
        }
    }
}
