package com.canal.center;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import com.canal.center.cache.TaskCache;
import com.canal.center.netty.server.DatacanalCenter;
import com.canal.center.selecter.impl.RandomChannelSelector;
import com.canal.center.selecter.intf.IChannelSelector;
import com.canal.center.thread.CheckHeartbeatThread;
import com.canal.center.thread.HandleCommandThread;
import com.canal.serializer.impl.JSONSerializer;
import com.canal.serializer.intf.ISerializer;
import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.Command;
import com.datacanal.common.util.CommonUtils;
import com.datacanal.common.util.ZkUtil;
import com.datacanal.zookeeper.lock.impl.ZkSimpleDistributedLock;
import com.datacanal.zookeeper.lock.intf.IDistributedLock;

import io.netty.channel.ChannelFuture;

/**
 * CDC中心管理器的启动类
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月31日 下午5:24:59
 */
@ImportResource("classpath:applicationContext.xml")
@Component
public class CanalCenterLauncher {
    
    public static final Logger LOG = LoggerFactory.getLogger(CanalCenterLauncher.class);
    
    @Value("${zookeeper.connection}")
    public String zkString;
    
    @Value("${server.port}")
    public int serverPort;
    
    @Value("${hearbeat.max.second}")
    private int maxHeartbeat;
    
    @Value("${check.hearbeat.second}")
    private int checkHearbeatPeriod;
    
    private IDistributedLock lock;
    
    private ZkClient zkClient;
    
    //canal center的server
    private DatacanalCenter datacanalCenterServer = DatacanalCenter.instance();
    
    //序列化
    ISerializer<Command> serializer = new JSONSerializer<>();
    
    //channel的选择器
    IChannelSelector selector = new RandomChannelSelector();
    
    public static void main(String[] args) {
        //设置未捕获异常的handler
        setGlobalUncaughtExceptionHandler();
        
        @SuppressWarnings("resource")
        ApplicationContext context = new AnnotationConfigApplicationContext(CanalCenterLauncher.class);
        CanalCenterLauncher launcher = context.getBean(CanalCenterLauncher.class);
        launcher.setup();
        
        launcher.datacanalCenterServer.setPort(launcher.serverPort);
        ChannelFuture channelFuture = null;
        try {
            channelFuture = launcher.datacanalCenterServer.start();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            System.exit(-1);
        }
        
        try {
            //将自己注册到zookeeper上去
            launcher.registToZookeeper(launcher.serverPort);
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
            System.exit(-1);
        }
        
        //load任务
        TaskCache taskCache = TaskCache.instance();
        taskCache.loadTask(launcher.zkClient);
        
        //心跳检测
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new CheckHeartbeatThread(launcher.maxHeartbeat), 0l, launcher.checkHearbeatPeriod, TimeUnit.SECONDS);
        
        //instance启动命令处理
        HandleCommandThread handleCommand = new HandleCommandThread(launcher.selector, launcher.serializer);
        Executors.newFixedThreadPool(1).execute(handleCommand);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Stop canal center.");
            }
        });
        
        LOG.info("Canal center start success.");
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
        }
    }
    
    /**
     * 
     * @param serverPort
     * @throws UnknownHostException
     */
    public void registToZookeeper(int serverPort) throws UnknownHostException {
        String localIp = CommonUtils.getLocalIp();
        StringBuilder childPath = new StringBuilder();
        childPath.append(localIp).append(":").append(serverPort);
        ZkUtil.createChildPath(zkClient, Consts.ZK_PATH_CENTER_SERVER, childPath.toString(), "", CreateMode.EPHEMERAL);
    }
    
    /**
     * 
     */
    public void setup() {
        zkClient = new ZkClient(zkString);
        if(!zkClient.exists(Consts.DISTRIBUTED_LOCK_BASE_PATH)) {
            ZkUtil.createPathRecursive(zkClient, Consts.DISTRIBUTED_LOCK_BASE_PATH);
        }
        
        if(!zkClient.exists(Consts.ZK_PATH_CENTER_SERVER)) {
            ZkUtil.createPathRecursive(zkClient, Consts.ZK_PATH_CENTER_SERVER);
        }
        
        if(!zkClient.exists(Consts.ZK_PATH_TASK)) {
            ZkUtil.createPathRecursive(zkClient, Consts.ZK_PATH_TASK);
        }
        
        //尝试自己做master,如果被强就堵塞
        preStart(zkClient, buildLockPath());
    }
    
    /**
     * 一直堵塞直到自己获取到锁
     * @param zkClient
     * @param lockPath
     */
    private void preStart(ZkClient zkClient, String lockPath) {
        lock = new ZkSimpleDistributedLock(lockPath, zkClient);
        lock.lock();
        LOG.info("I am master.");
    }
    
    /**
     * 构造锁zookeeper目录
     * @return
     */
    private String buildLockPath() {
        StringBuilder lockPath = new StringBuilder();
        lockPath.append(Consts.DISTRIBUTED_LOCK_BASE_PATH)
                .append(Consts.ZK_PATH_SEPARATOR)
                .append(Consts.DISTRIBUTED_LOCK_CANAL_CENTER);
        return lockPath.toString();
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
