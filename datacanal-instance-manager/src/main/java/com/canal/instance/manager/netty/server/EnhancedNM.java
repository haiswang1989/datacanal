package com.canal.instance.manager.netty.server;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.canal.instance.manager.thread.HeartbeatThread;
import com.canal.serializer.impl.JSONSerializer;
import com.canal.serializer.intf.ISerializer;
import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.Command;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * 增强型NodeManager的Server
 * 当Canal server挂掉,再重启可以重新连上 
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月7日 下午3:06:06
 */
public class EnhancedNM implements Runnable {
    
    public static final Logger LOG = LoggerFactory.getLogger(EnhancedNM.class);
    
    //nodemanager的server
    private NodeManager nmServer;
    
    private ZkClient zkClient;
    
    private ScheduledExecutorService scheduledES;
    
    private ISerializer<Command> serializer;
    
    private String nodeId;
    
    private int heartbeatSecond;
    
    private String startInstanceShell;
    
    public EnhancedNM(ZkClient zkClientArg, String nodeIdArg, int heartbeatSecondArg, String startInstanceShellArg) {
        nmServer = NodeManager.instance();
        scheduledES = Executors.newScheduledThreadPool(1);
        serializer = new JSONSerializer<>();
        this.zkClient = zkClientArg;
        this.nodeId = nodeIdArg;
        this.heartbeatSecond = heartbeatSecondArg;
        this.startInstanceShell = startInstanceShellArg;
    }
    
    @Override
    public void run() {
        while(true) {
            String centerServer = getCenterIpPort();
            //当前没有center注册在zookeeper中
            //sleep一段时间再去检查
            if(null==centerServer) {
                LOG.error("Canal server is not no line.");
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                }
                continue;
            } else {
                LOG.info("Canal server is on line, info : {}", centerServer);
                //center启动起来了
                String[] ipPort = centerServer.split(":");
                String ip = ipPort[0];
                int port = Integer.parseInt(ipPort[1]);
                try {
                    //连接上center
                    ChannelFuture future = nmServer.connect(ip, port, startInstanceShell, zkClient);
                    Channel channel = future.channel();
                    scheduledES.scheduleAtFixedRate(new HeartbeatThread(channel, serializer, this.nodeId), 0, this.heartbeatSecond, TimeUnit.SECONDS);
                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    continue;
                } catch (Exception e) {
                    if(e instanceof ConnectException) {
                        //canal server down机以后,结点不会立即消失
                        //在这个"真空期"可能会正常拿到server的地址信息,但是在连接的时候会失败
                        LOG.error("Connect to {} failed.", centerServer);
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException ex) {
                        }
                    } else {
                        throw e;
                    }
                } finally {
                    //TODO 当出现"ConnectException"这边会重复关闭,创建
                    //关闭心跳任务
                    scheduledES.shutdownNow();
                    //重新开启一个循环定时执行器
                    scheduledES = Executors.newScheduledThreadPool(1);
                }
            }
        }
        
    }

    /**
     * 获取已注册的center
     * @return
     */
    public String getCenterIpPort() {
        List<String> servers = zkClient.getChildren(Consts.DATACANAL_CANAL_SERVER);
        if(null==servers || 0==servers.size()) {
            return null;
        }
        
        return servers.get(0);
    }
}
