package com.canal.instance.code;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.OpenReplicator;
import com.google.code.or.binlog.BinlogParser;
import com.google.code.or.binlog.BinlogParserListener;

import lombok.Setter;

/**
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
                canConnect = future.get(trySecond, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            } catch (TimeoutException e) {
            } finally {
                //关闭尝试连接线程
                tryConnect.setNeedTry(false);
                try {
                    TimeUnit.SECONDS.sleep(4L);
                } catch (InterruptedException e) {
                }
                es.shutdownNow();
            }
        }
        
        @Override
        public void onStop(BinlogParser parser) {
            LOG.warn("onStop, canConnect : {}", canConnect);
            stopQuietly(0, TimeUnit.MILLISECONDS);
            if(canConnect) { //连接已经恢复,重新启动
                LOG.info("Try to restart.");
                try {
                    openReplicatorPlus.start();
                } catch (Exception e) {
                    LOG.error("Restart failed", e);
                    System.exit(-1);
                }
            } else { //等待了指定的时间,连接没有恢复确定宕机
                LOG.info("Try to start on other slave.");
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
                    TimeUnit.SECONDS.sleep(3);
                } catch(Exception e) {
                }
            }
        }
        
        return false;
    }
}