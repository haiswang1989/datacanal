package com.canal.instance;

import java.util.HashSet;
import java.util.Set;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.canal.instance.exception.ParamException;
import com.canal.instance.handler.keeper.PositionKeeper;
import com.canal.instance.handler.keeper.SensitiveTablesKeeper;
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

    @Autowired
    private CDCInstanceListener listener;
    
    @Value("${zookeeper.connection}")
    private String zkString;
    
    private OpenReplicator openReplicator;
    private ZkClient zkClient;
    
    //DB的host
    private String dbHost;
    //DB的端口
    private int dbPort;
    //DB的用户名
    private String username;
    //DB的密码
    private String password;
    //DB的名称
    private String dbName;
    //抽取表的名称,多个以逗号隔开
    private String sensitiveTables;
    
    //binlog的position同步到ZK的时间间隔
    private int positionSyncZkPeriod;
    private int DEFAULT_POSITION_SYNC_ZK_PERIOD = 5; //默认是5秒
    
    
    
    /**
     * java CDCEngine -h 192.168.56.101 -p 3006 -u root -pw 123456 -n test -st person -sp 5
     * 
     * -h   DB的host(必须)
     * -p   DB的port(必须)
     * -u   DB的username(必须)
     * -pw  DB的password(必须)
     * -n   DB的名称(必须)
     * -st  需要抽取DB表的名称
     * -sp  SYNC Position到ZK的时间间隔
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        @SuppressWarnings("resource")
        ApplicationContext context = new AnnotationConfigApplicationContext(CDCEngine.class);
        CDCEngine engine = context.getBean(CDCEngine.class);
        
        try {
            engine.parseArgs(args);
            engine.setup(engine.dbHost, engine.dbPort, engine.username, engine.password, engine.dbName, engine.zkString);
            TableInfoKeeper.init();
            //设置敏感表
            SensitiveTablesKeeper.setSensitiveTables(engine.parseSensitiveTables(engine.sensitiveTables));
            //position
            PositionKeeper.setPositionSyncZkPeriod(engine.positionSyncZkPeriod);
            PositionKeeper.setZkClient(engine.zkClient);
            PositionKeeper.init();
            engine.start();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            System.exit(-1);
        }
    }
    
    /**
     * 
     * @param sensitiveTables
     * @return
     */
    public Set<String> parseSensitiveTables(String sensitiveTables) {
        Set<String> retSensitiveTables = new HashSet<>();
        for (String sensitiveTable : sensitiveTables.split(",")) {
            retSensitiveTables.add(sensitiveTable);
        }
        
        return retSensitiveTables;
    }
    
    /**
     * 解析main函数的参数
     * @param args
     * @throws ParseException
     * @throws ParamException
     */
    public void parseArgs(String[] args) throws ParseException, ParamException {
        final CommandLineParser parser = new PosixParser();
        final Options options = new Options();
        CommandLine cmdLine = parser.parse(options, args);
        
        if(cmdLine.hasOption("h")) { //host
            dbHost = cmdLine.getOptionValue("h");
        } else {
            throw new ParamException("Db host must provide.");
        }
        
        if(cmdLine.hasOption("p")) { //port
            try {
                dbPort = Integer.parseInt(cmdLine.getOptionValue("p"));
            } catch(NumberFormatException e) {
                throw new ParamException("Db port must right to provide.", e);
            }
        } else {
            throw new ParamException("Db port must provide.");
        }
        
        if(cmdLine.hasOption("u")) { //username
            username = cmdLine.getOptionValue("u");
        } else {
            throw new ParamException("Db username must provide.");
        }
        
        if(cmdLine.hasOption("pw")) { //password
            password = cmdLine.getOptionValue("pw");
        } else {
            throw new ParamException("Db password must provide.");
        }
        
        if(cmdLine.hasOption("n")) { //Db的名称
            dbName = cmdLine.getOptionValue("n");
        } else {
            throw new ParamException("Db name must provide.");
        }
         
        if(cmdLine.hasOption("st")) { //敏感表
            dbName = cmdLine.getOptionValue("st");
        } else {
            throw new ParamException("Db sensitive tables must provide.");
        }
        
        if(cmdLine.hasOption("sp")) { //同步position到zk的时间间隔
            positionSyncZkPeriod = Integer.parseInt(cmdLine.getOptionValue("sp"));
        } else {
            positionSyncZkPeriod = DEFAULT_POSITION_SYNC_ZK_PERIOD;
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
     * @param dbName
     */
    private void setup(String host, int port, String username, String password, String dbName, String zkString) {
        
        zkClient = new ZkClient(zkString);
        
        //监听目标数据库的datasource
        DruidDataSource datasource = createDruidDatasource(host, port, username, password, dbName);
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
     * @param dbName
     * @return
     */
    private DruidDataSource createDruidDatasource(String host, int port, String username, String password, String dbName) {
        DruidDataSource druidDatasource = new DruidDataSource();
        druidDatasource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDatasource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName);
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
