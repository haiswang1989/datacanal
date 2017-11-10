package com.canal.instance.manager;

import java.net.UnknownHostException;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import com.canal.instance.manager.netty.server.EnhancedNM;
import com.datacanal.common.constant.Consts;
import com.datacanal.common.util.CommonUtils;
import com.datacanal.common.util.ZkUtil;

/**
 * nodemanager的启动类
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 上午11:40:05
 */
@ImportResource("classpath:applicationContext.xml")
@Component
public class NodeManagerLaucher {
    
    public static final Logger LOG = LoggerFactory.getLogger(NodeManagerLaucher.class);
    
    @Value("${zookeeper.connection}")
    public String zkString;
    
    @Value("${node.id}")
    private String nodeId;
    
    @Value("${heartbeat.second}")
    private int heartbeatSecond;
    
    private ZkClient zkClient;
    
    public static void main(String[] args) {
        //注册全局未捕获异常处理handler
        setGlobalUncaughtExceptionHandler();
        @SuppressWarnings("resource")
        ApplicationContext context = new AnnotationConfigApplicationContext(NodeManagerLaucher.class);
        final NodeManagerLaucher laucher = context.getBean(NodeManagerLaucher.class);
        laucher.setup();
        
        //将自己注册到zk上
        try {
            laucher.registToZookeeper();
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
            System.exit(-1);
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Stop node manager.");
            }
        });
        
        EnhancedNM enhancedNMServer = new EnhancedNM(laucher.zkClient, laucher.nodeId, laucher.heartbeatSecond);
        Thread t1 = new Thread(enhancedNMServer);
        t1.start();
        
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public void setup() {
        zkClient = new ZkClient(zkString);
        if(!zkClient.exists(Consts.DATACANAL_NODE)) {
            ZkUtil.createPathRecursive(zkClient, Consts.DATACANAL_NODE);
        }
    }
    
    /**
     * 将当前运行结点注册到zookeeper上
     * @throws UnknownHostException
     */
    public void registToZookeeper() throws UnknownHostException {
        String localIp = CommonUtils.getLocalIp();
        ZkUtil.createChildPath(zkClient, Consts.DATACANAL_NODE, localIp, "", CreateMode.EPHEMERAL);
    }
    
    /**
     * 全局未catch的异常处理handler
     */
    private static void setGlobalUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.error("UnCaughtException", e);
            }
        });
    }
}
