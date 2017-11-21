package com.canal.instance.manager.code.netty.handler;


import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datacanal.common.model.Command;
import com.datacanal.common.model.EventType;
import com.datacanal.common.util.CommonUtils;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理服务端发过来的启动instance的指令
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月6日 下午3:08:01
 */
public class InstanceStartHandler extends ChannelHandlerAdapter {
    
    public static final Logger LOG = LoggerFactory.getLogger(InstanceStartHandler.class);
    
    //启动脚本所在的位置
    private String startInstanceShell;
    
    //zk
    private ZkClient zkClient;
    
    public InstanceStartHandler(String startInstanceShellArg, ZkClient zkClientArg) {
        this.startInstanceShell = startInstanceShellArg;
        this.zkClient = zkClientArg;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command)msg;
        EventType eventType = command.getEventType();
        if(eventType == EventType.INSTANCE_START) {
            String physicsTable = (String)command.getObj();
            LOG.info("Get start instance command ,table name : " + physicsTable);
            startInstance(physicsTable);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
    
    /**
     * 处理command,启动instance
     * @param physicsTable
     */
    public void startInstance(String physicsTable) {
        if(zkClient.exists(physicsTable)) {
            String cmd = buildCmd(physicsTable);
            LOG.info("Cmd : {}", cmd);
            CommonUtils.doExecCmd(cmd);
            //这边需要sleep一段时间,可能一开始刚启动的时候有PID,等会儿就没了
            //但是sleep又会导致整个通信周期太长
            /*
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
            }
            */
            
            //return checkIsOnline(dbInfo);
        } 
        
        //LOG.error("Start instance failed, path : [{}] not exist.", physicsTable);
        //return false;
    }
    
    /**
     * 检查是否启动成功
     * @param dbInfo
     * @return
     */
    /*
    public boolean checkIsOnline(DbInfo dbInfo) {
        StringBuilder pidFileBuidler = new StringBuilder();
        pidFileBuidler.append(this.pidPath).append(File.separator).append(dbInfo.getHost())
            .append("_").append(dbInfo.getDbName()).append(".pid");
        
        File pidFile = new File(pidFileBuidler.toString());
        String pid = null;
        if(pidFile.exists()) {
            try {
                pid = getPid(pidFile);
                return checkPidInProcess(pid);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } 
        
        return false;
    }
    */
    /**
     * 通过Pid文件获取PID
     * @param pidFile
     * @return
     * @throws IOException
     */
    /*
    private String getPid(File pidFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pidFile)))) {
            return br.readLine();
        } 
    }
    */
    
    /**
     * check PID是否在线
     * @param pid
     * @return
     * @throws IOException
     */
    /*
    private boolean checkPidInProcess(String pid) throws IOException {
        final Process process = Runtime.getRuntime().exec("jps");
        InputStream inputStream = process.getInputStream();
        new Thread(new Runnable() { //这边把错误流读取掉,不然可能出现堵塞
            @Override
            public void run() {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String lineContent = null;
                    while(null!=(lineContent=br.readLine())) {
                        LOG.info(lineContent);
                    }
                } catch (IOException e) {
                }
            }
        }).start();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        while(null!=(line=br.readLine())) {
            if(line.startsWith(pid)) {
                return true;
            }
        }
        
        return false;
    }
    */
    
    /**
     * 构造启动脚本
     * @param dbInfo
     */
    private String buildCmd(String taskNodePath) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("sh ").append(startInstanceShell).append(" ").append(" %s");
        return String.format(cmd.toString(), taskNodePath);
    }
}
