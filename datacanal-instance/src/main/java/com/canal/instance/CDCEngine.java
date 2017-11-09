package com.canal.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.canal.instance.handler.keeper.TableInfoKeeper;
import com.canal.instance.listener.CDCInstanceListener;
import com.canal.instance.mysql.DbMetadata;
import com.datacanal.common.model.BinlogMasterStatus;
import com.google.code.or.OpenReplicator;

/**
 * 启动binlog日志抽取引擎
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午3:38:25
 */
@Service
@ImportResource("classpath:applicationContext.xml")
public class CDCEngine {
    
    public static final Logger LOG = LoggerFactory.getLogger(CDCEngine.class);
    
    private OpenReplicator openReplicator;
    
    @Autowired
    private CDCInstanceListener listener;
    
    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String username = args[2];
        String password = args[3];
        
        @SuppressWarnings("resource")
        ApplicationContext context = new AnnotationConfigApplicationContext(CDCEngine.class);
        CDCEngine engine = context.getBean(CDCEngine.class);
        try {
            engine.setup(host, port, username, password);
            TableInfoKeeper.init();
            engine.start();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            System.exit(-1);
        }
    }
    
    /**
     * 
     * @throws Exception
     */
    private void start() throws Exception {
        openReplicator.start();
    }
    
    /**
     * 
     * @param host
     * @param port
     * @param username
     * @param password
     */
    private void setup(String host, int port, String username, String password) {
        //监听目标数据库的datasource
        DruidDataSource datasource = createDruidDatasource(host, port, username, password);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
        
        DbMetadata.setJdbcTemplate(jdbcTemplate);
        
        openReplicator = new OpenReplicator();
        openReplicator.setUser(username);
        openReplicator.setPassword(password);
        openReplicator.setHost(host);
        openReplicator.setPort(port);
        
        //获取master生成的binlog的信息
        BinlogMasterStatus binlogMasterStatus = DbMetadata.getBinlongMasterStatus();
        openReplicator.setBinlogFileName(binlogMasterStatus.getBinlogName());
        openReplicator.setBinlogPosition(binlogMasterStatus.getPosition());
        openReplicator.setBinlogEventListener(listener);
    }
    
    /**
     * 目标数据库的datasource
     * @param host
     * @param port
     * @param username
     * @param password
     * @return
     */
    private DruidDataSource createDruidDatasource(String host, int port, String username, String password) {
        DruidDataSource druidDatasource = new DruidDataSource();
        druidDatasource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDatasource.setUrl("jdbc:mysql://" + host + ":" + port);
        druidDatasource.setUsername(username);
        druidDatasource.setPassword(password);
        druidDatasource.setMaxActive(1);
        druidDatasource.setMinIdle(1);
        druidDatasource.setInitialSize(1);
        druidDatasource.setMaxWait(10000l);
        druidDatasource.setMinEvictableIdleTimeMillis(300000l);
        druidDatasource.setTimeBetweenEvictionRunsMillis(60000l);
        return druidDatasource;
    }
}
