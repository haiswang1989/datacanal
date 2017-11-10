package com.canal.instance;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.zookeeper.CreateMode;
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
import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.BinlogMasterStatus;
import com.datacanal.common.util.CommonUtils;
import com.datacanal.common.util.ZkUtil;
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
    
    //binlog的position同步到ZK的时间间隔
    @Value("${sync.period}")
    private int positionSyncZkPeriod;
    
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
    //该分片
    private String zkPath;
    
    
    /**
     * java CDCEngine -h 192.168.56.101 -p 3306 -u root -pw 123456 -n test -st person -sp 5 -zp /datacanal/task/person/person-1
     * 
     * -h   DB的host(必须)
     * -p   DB的port(必须)
     * -u   DB的username(必须)
     * -pw  DB的password(必须)
     * -n   DB的名称(必须)
     * -st  需要抽取DB表的名称
     * -sp  SYNC Position到ZK的时间间隔
     * -zp  分片在ZK上的路径
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        @SuppressWarnings("resource")
        ApplicationContext context = new AnnotationConfigApplicationContext(CDCEngine.class);
        CDCEngine engine = context.getBean(CDCEngine.class);
        
        try {
            engine.parseArgs(args);
            engine.setup();
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
     * @throws UnknownHostException
     */
    private void setup() throws UnknownHostException {
        zkClient = new ZkClient(zkString);
        //将自己注册到zookeeper上面
        registToZookeeper(zkPath);
        
        initJdbc();
        initOpenReplicator();
        
        TableInfoKeeper.init();
        //设置敏感表
        SensitiveTablesKeeper.setSensitiveTables(parseSensitiveTables(sensitiveTables));
        //position
        PositionKeeper.setPositionSyncZkPeriod(positionSyncZkPeriod);
        PositionKeeper.setZkClient(zkClient);
        PositionKeeper.init(zkPath);
        
    }
    
    /**
     * 初始化JDBC
     */
    private void initJdbc() {
        //监听目标数据库的datasource
        DruidDataSource datasource = createDruidDatasource(dbHost, dbPort, username, password, dbName);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
        DbMetadata.setJdbcTemplate(jdbcTemplate);
    }
    
    /**
     * 
     */
    private void initOpenReplicator() {
        openReplicator = new OpenReplicator();
        openReplicator.setUser(username);
        openReplicator.setPassword(password);
        openReplicator.setHost(dbHost);
        openReplicator.setPort(dbPort);
        
        //获取master生成的binlog的信息
        BinlogMasterStatus binlogMasterStatus = DbMetadata.getBinlongMasterStatus();
        openReplicator.setBinlogFileName(binlogMasterStatus.getBinlogName());
        
        openReplicator.setBinlogPosition(binlogMasterStatus.getPosition());
        //TODO
        //openReplicator.setBinlogPosition(PositionKeeper.getPosition());
        openReplicator.setBinlogEventListener(listener);
    }
    
    /**
     * 
     * @param path
     * @param child
     * @throws UnknownHostException 
     */
    private void registToZookeeper(String path) throws UnknownHostException {
        String localIp = CommonUtils.getLocalIp();
        StringBuilder registPath = new StringBuilder();
        registPath.append(path).append(Consts.ZK_PATH_SEPARATOR).append(Consts.DATACANAL_TASK_INSTANCE);
        ZkUtil.createChildPath(zkClient, registPath.toString(), localIp, "", CreateMode.EPHEMERAL);
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
    
    /**
     * 
     * @param sensitiveTables
     * @return
     */
    private Set<String> parseSensitiveTables(String sensitiveTables) {
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
    private void parseArgs(String[] args) throws ParseException, ParamException {
        final CommandLineParser parser = new PosixParser();
        final Options options = new Options();
        
        options.addOption("h", true, "DB的host(必须)");
        options.addOption("p", true, "DB的port(必须)");
        options.addOption("u", true, "DB的username(必须)");
        options.addOption("pw", true, "DB的password(必须)");
        options.addOption("n", true, "DB的名称(必须)");
        options.addOption("st", true, "需要抽取DB表的名称");
        options.addOption("sp", true, "SYNC Position到ZK的时间间隔");
        options.addOption("zp", true, "分片在zk的路径");
        
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
            sensitiveTables = cmdLine.getOptionValue("st");
        } else {
            throw new ParamException("Db sensitive tables must provide.");
        }
        
        if(cmdLine.hasOption("zp")) {
            zkPath = cmdLine.getOptionValue("zp");
        } else {
            throw new ParamException("Db zk Path must provide.");
        }
    }
}
