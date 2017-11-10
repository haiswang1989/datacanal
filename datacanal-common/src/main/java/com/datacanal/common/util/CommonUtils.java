package com.datacanal.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

import com.datacanal.common.constant.Consts;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午3:30:02
 */
public class CommonUtils {
    
    /**
     * 
     * @return
     * @throws UnknownHostException 
     */
    public static String getLocalIp() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        return localHost.getHostAddress();
    }
    
    /**
     * 获取input1相对于input2添加的元素
     * 
     * @param input1
     * @param input2
     * @return
     */
    public static HashSet<String> comparaHashsetToAdd(HashSet<String> input1, HashSet<String> input2) {
        if(null==input1 || 0==input1.size()) {
            return new HashSet<>();
        }
        
        for (String string : input2) {
            input1.remove(string);
        }
        
        return input1;
    }
    
    /**
     * 
     * @param parentPath
     * @param paths
     * @return
     */
    public static HashSet<String> convertToFullPath(String parentPath, HashSet<String> paths) {
        if(0==paths.size()) {
            return paths;
        }
        
        StringBuilder fullPathBuilder = new StringBuilder();
        HashSet<String> fullPaths = new HashSet<>();
        for (String path : paths) {
            fullPathBuilder.setLength(0);
            fullPathBuilder.append(parentPath).append(Consts.ZK_PATH_SEPARATOR).append(path);
            fullPaths.add(fullPathBuilder.toString());
        }
        
        return fullPaths;
    }
}
