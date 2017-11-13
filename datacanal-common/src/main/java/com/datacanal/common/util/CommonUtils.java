package com.datacanal.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacanal.common.constant.Consts;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 下午3:30:02
 */
public class CommonUtils {
    
    public static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);
    
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
    
    /**
     * 
     * @param cmd
     * @return
     */
    public static boolean doExecCmd(String cmd) {
        LOG.info("Exec cmd [{}] start.", cmd);
        try {
            final Process process = Runtime.getRuntime().exec(cmd);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try(BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String lineContent = null;
                        while(null!=(br.readLine())) {
                            LOG.info("std : " + lineContent);
                        }
                    } catch (IOException e) {
                    }
                }
            }).start();
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try(BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String lineContent = null;
                        while(null!=(br.readLine())) {
                            LOG.info("err : " + lineContent);
                        }
                    } catch (IOException e) {
                    }
                }
            }).start();
            
            process.waitFor();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        
        LOG.info("Exec cmd [{}] end.", cmd);
        return true;
    }
    
}
