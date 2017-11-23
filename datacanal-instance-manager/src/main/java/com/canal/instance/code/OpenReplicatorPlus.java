package com.canal.instance.code;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.canal.instance.code.handler.keeper.CommonKeeper;
import com.datacanal.common.model.DbNode;
import com.google.code.or.OpenReplicator;
import com.google.code.or.binlog.BinlogParser;
import com.google.code.or.binlog.BinlogParserListener;

import lombok.Setter;

/**
 * OpenReplicator的加强版
 * 1:可以从mysql的重启中恢复
 * 2:当slave挂掉以后,可以切换到其他slave上面
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月21日 下午2:26:53
 */
public class OpenReplicatorPlus extends OpenReplicator {
    
    public static final Logger LOG = LoggerFactory.getLogger(OpenReplicatorPlus.class);
    
    private String host;
    private int port;
    private String dbName;
    private String username;
    private String password;
    private long tryConnectTimeout;
    
    public OpenReplicatorPlus(String hostArg, int portArg, String dbNameArg, String usernameArg, String passwordArg, long tryConnectTimeoutArg) {
        this.host = hostArg;
        this.port = portArg;
        this.dbName = dbNameArg;
        this.username = usernameArg;
        this.password = passwordArg;
        this.tryConnectTimeout = tryConnectTimeoutArg;
    }
    
    @Override
    public void start() throws Exception {
        //
        if(!this.running.compareAndSet(false, true)) {
            return;
        }

        //
        if(this.transport == null) this.transport = getDefaultTransport();
        this.transport.connect(this.host, this.port);
        
        //
        if(this.binlogParser == null)
            this.binlogParser = getDefaultBinlogParser();

        setupChecksumState();
        setupHeartbeatPeriod();
        setupSlaveUUID();
        dumpBinlog();

        this.binlogParser.setBinlogFileName(this.binlogFileName);

        this.binlogParser.setEventListener(this.binlogEventListener);
        this.binlogParser.addParserListener(new BinlogParserListenerPlus(tryConnectTimeout, this));
        this.binlogParser.start();
    }
    
    class BinlogParserListenerPlus extends BinlogParserListener.Adapter {
        
        private OpenReplicatorPlus openReplicatorPlus;
        
        private long trySecond;
        
        private boolean canConnect;
        
        public BinlogParserListenerPlus(long trySecondArg, OpenReplicatorPlus openReplicatorPlusArg) {
            this.trySecond = trySecondArg;
            this.openReplicatorPlus = openReplicatorPlusArg;
        }
        
        @Override
        public void onException(BinlogParser parser, Exception exception) {
            //在解析过程中出现异常 
            //mysql断开以后会出现：java.lang.RuntimeException: EOFPacket[packetMarker=254,warningCount=0,serverStatus=2050]
            LOG.warn("onException...");
            TryConnect tryConnect = new TryConnect(host, port, dbName, username, password);
            ExecutorService es = Executors.newFixedThreadPool(1);
            Future<Boolean> future = es.submit(tryConnect);
            canConnect = false;
            try {
                if(CommonKeeper.isExtractMaster()) {
                    //如果是抽取的master,那么就一直尝试,直到master可以连接
                    //在Mysql的HA的配置中,如果Master挂掉了,会用一个Slave来顶替Master
                    //使用的方式是在Slave中添加一个IP地址,该IP地址就是原Master的IP地址
                    canConnect = future.get();
                } else {
                    //如果抽取的是slave,那么就尝试指定的时间,如果到了指定的时间还是没有成功
                    //那么就直接认为该slave宕机,漂移到其他slave上面执行
                    canConnect = future.get(trySecond, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                LOG.error(e.getMessage(), e);
            } catch (TimeoutException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                //关闭尝试连接线程
                tryConnect.setNeedTry(false);
                try {
                    TimeUnit.SECONDS.sleep(4L);
                } catch (InterruptedException e) {
                }
                //关闭线程池
                es.shutdownNow();
            }
        }
        
        @Override
        public void onStop(BinlogParser parser) {
            LOG.warn("onStop, canConnect : {}", canConnect);
            stopQuietly(0, TimeUnit.MILLISECONDS);
            if(canConnect) { 
                //连接已经恢复,重新启动(不管是master或者是slave)
                LOG.info("Try to restart.");
                try {
                    openReplicatorPlus.start();
                } catch (Exception e) {
                    LOG.error("Restart failed", e);
                    System.exit(-1);
                }
            } else { 
                //等待了指定的时间,连接没有恢复,确定宕机,尝试在其他的slave上面启动
                LOG.info("Try restart on other slave.");
                List<DbNode> slaves = CommonKeeper.getSlaves();
                DbNode currNode = CommonKeeper.getCurrentSlave();
                CDCEngine engine = CommonKeeper.getEngine();
                boolean isStart = false;
                for (DbNode dbNode : slaves) {
                    if(currNode.equals(dbNode)) {
                        continue;
                    }
                    
                    LOG.info("Try restart on [{}]", dbNode.toString());
                    
                    try {
                        engine.changableInit(dbNode);
                    } catch(Exception e) {
                        LOG.error(e.getMessage(), e);
                        continue;
                    }
                    
                    try {
                        engine.start();
                        //重置当前的slave
                        CommonKeeper.setCurrentSlave(dbNode);
                        isStart = true;
                        LOG.info("Dump restart success, on [{}]", dbNode.toString());
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                
                //如果没有找到合适的DB进行抽取,那么直接退出JVM
                if(!isStart) {
                    LOG.error("No useful db, JVM will exit.");
                    System.exit(-1);
                }
            }
        }
    }
}

/**
 * 不断尝试连接数据库,如果连接成功了,那么返回true
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月21日 下午3:16:02
 */
class TryConnect implements Callable<Boolean> {
    
    //外部控制是否停止尝试
    @Setter
    private volatile boolean needTry;
    
    private String host;
    private int port;
    private String dbName;
    private String username;
    private String password;
    
    public TryConnect(String hostArg, int portArg, String dbNameArg, String usernameArg, String passwordArg) {
        this.host = hostArg;
        this.port = portArg;
        this.dbName = dbNameArg;
        this.username = usernameArg;
        this.password = passwordArg;
        needTry = true;
    }
    
    @Override
    public Boolean call() throws Exception {
        String url = "jdbc:mysql://"+host+":"+port+"/"+ dbName;
        Connection conn = null;
        while(needTry) {
            try {
                conn = DriverManager.getConnection(url, username, password);
            } catch(SQLException e) {
            }
            
            if(null!=conn) { //如果成功获取到连接,那么直接return true
                try {
                    conn.close();
                } catch(Exception e) {
                }
                return true;
            } else {
                try {
                    TimeUnit.SECONDS.sleep(3L);
                } catch(Exception e) {
                }
            }
        }
        
        return false;
    }
}