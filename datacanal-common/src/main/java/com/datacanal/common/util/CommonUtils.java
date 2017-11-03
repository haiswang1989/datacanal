package com.datacanal.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
}
