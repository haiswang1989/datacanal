package com.canal.instance.manager.netty.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.datacanal.common.model.Command;
import com.datacanal.common.model.DbInfo;
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
    
    //pid文件所在的路径
    private String pidPath;
    
    public InstanceStartHandler(String startInstanceShellArg, ZkClient zkClientArg, String pidPathArg) {
        this.startInstanceShell = startInstanceShellArg;
        this.zkClient = zkClientArg;
        this.pidPath = pidPathArg;
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
    public boolean startInstance(String physicsTable) {
        if(zkClient.exists(physicsTable)) {
            String jsonString = zkClient.readData(physicsTable);
            DbInfo dbInfo = JSON.parseObject(jsonString, DbInfo.class);
            String cmd = buildCmd(dbInfo);
            LOG.info("Cmd : {}", cmd);
            CommonUtils.doExecCmd(cmd);
            return checkIsOnline(dbInfo);
        } 
        
        LOG.error("Start instance failed, path : [{}] not exist.", physicsTable);
        return false;
    }
    
    /**
     * 检查是否启动成功
     * @param dbInfo
     * @return
     */
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
    
    /**
     * 通过Pid文件获取PID
     * @param pidFile
     * @return
     * @throws IOException
     */
    private String getPid(File pidFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pidFile)))) {
            return br.readLine();
        } 
    }
    
    /**
     * check PID是否在线
     * @param pid
     * @return
     * @throws IOException
     */
    private boolean checkPidInProcess(String pid) throws IOException {
        InputStream inputStream = Runtime.getRuntime().exec("jps").getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        while(null!=(line=br.readLine())) {
            if(line.startsWith(pid)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 构造启动脚本
     * @param dbInfo
     */
    private String buildCmd(DbInfo dbInfo) {
        StringBuilder cmd = new StringBuilder();
        cmd.append("sh ").append(startInstanceShell).append(" ").append(" %s %d %s %s %s %s %s");
        return String.format(cmd.toString(), dbInfo.getHost(), dbInfo.getPort(), dbInfo.getUsername(), 
                dbInfo.getPassword(), dbInfo.getDbName(), StringUtils.join(dbInfo.getSensitiveTables(), ","), dbInfo.getZkPath());
    }
}
