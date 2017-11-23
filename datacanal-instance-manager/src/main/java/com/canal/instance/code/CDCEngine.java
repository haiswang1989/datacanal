package com.canal.instance.code;

import java.net.UnknownHostException;
import java.util.List;
import org.I0Itec.zkclient.ZkClient;
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
import com.alibaba.fastjson.JSON;
import com.canal.instance.code.handler.keeper.PositionKeeper;
import com.canal.instance.code.handler.keeper.CommonKeeper;
import com.canal.instance.code.handler.keeper.TableInfoKeeper;
import com.canal.instance.code.listener.CDCInstanceListener;
import com.canal.instance.code.mysql.DbMetadata;
import com.canal.instance.code.zklistener.StatusListener;
import com.datacanal.common.constant.Consts;
import com.datacanal.common.model.BinlogMasterStatus;
import com.datacanal.common.model.DbInfo;
import com.datacanal.common.model.DbNode;
import com.datacanal.common.model.Status;
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
    
    @Value("${node.id}")
    private int nodeId;
    
    @Value("${mysql.tryconnect.timeout}")
    private long tryConnectTimeout;
    
    private OpenReplicator openReplicator;
    private ZkClient zkClient;
    
    //binlog的name
    private String binlogFileName;
    
    //任务的zk目录
    private String taskNodePath;
    
    /**
     * java CDCEngine ${taskNodePath} 
     * @param args
     */
    public static void main(String[] args) {
        
        @SuppressWarnings("resource")
        ApplicationContext context = new AnnotationConfigApplicationContext(CDCEngine.class);
        CDCEngine engine = context.getBean(CDCEngine.class);
        if(null==args || 0==args.length) {
            LOG.error("Must input task node path.");
            return;
        }
        
        engine.taskNodePath = args[0];
        try {
            engine.setup(engine.taskNodePath, engine);
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
    public void start() throws Exception {
        openReplicator.start();
    }
    
    /**
     * 获取需要抽取的DbNode
     * @return
     */
    private DbNode getUseDbNode(DbInfo dbInfo) {
        DbNode useDbNode = null;
        boolean isUseMaster = dbInfo.isUseMaster();
        if(isUseMaster) {
            //用Master进行抽取
            useDbNode = dbInfo.getMaster();
        } else {
            //用Slave进行抽取
            useDbNode = dbInfo.getSlaves().get(0);
            //设置当前使用的slave
            CommonKeeper.setCurrentSlave(useDbNode);
        }
        
        return useDbNode;
    }
    
    /**
     * 
     * @throws UnknownHostException
     */
    public void setup(String taskNodePath, CDCEngine engine) {
        zkClient = new ZkClient(zkString);
        String jsonString = zkClient.readData(taskNodePath);
        DbInfo dbInfo = JSON.parseObject(jsonString, DbInfo.class);
        DbNode useDbNode = getUseDbNode(dbInfo);
        
        immutableInit(dbInfo);
        changableInit(useDbNode, false);
        
        //
        CommonKeeper.setEngine(engine);
        
        //将自己注册到zookeeper上面
        registToZookeeper(taskNodePath);
    }
    
    /**
     * 出现slave切换,不需要变化的部分的信息的设置
     * @param dbInfo
     */
    public void immutableInit(DbInfo dbInfo) {
        //设置task的db信息
        CommonKeeper.init(dbInfo);
        //position
        PositionKeeper.setPositionSyncZkPeriod(positionSyncZkPeriod);
        PositionKeeper.setZkClient(zkClient);
        PositionKeeper.init(taskNodePath);
    }
    
    /**
     * 如果出现slave切换,需要变化的部分信息的设置
     * @param useDbNode
     * @param reflashPosition
     */
    public void changableInit(DbNode useDbNode, boolean reflashPosition) {
        //初始化DB的信息
        initJdbc(useDbNode);
        //初始化dump binlog对象
        initOpenReplicator(useDbNode, reflashPosition);
        //设置抽取的binlog
        TableInfoKeeper.init(binlogFileName);
    }
    
    /**
     * 初始化JDBC
     * @param useDbNode
     */
    private void initJdbc(DbNode useDbNode) {
        //监听目标数据库的datasource
        DruidDataSource datasource = createDruidDatasource(useDbNode.getHost(), useDbNode.getPort(), useDbNode.getUsername(), useDbNode.getPassword(), useDbNode.getDbName());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
        DbMetadata.setJdbcTemplate(jdbcTemplate);
    }
    
    /**
     * 
     * @param useDbNode
     * @param reflashPosition 是否从数据库那最新的binlog的position信息 true:数据库拿 false:zk获取
     */
    private void initOpenReplicator(DbNode useDbNode, boolean reflashPosition) {
        openReplicator = new OpenReplicatorPlus(useDbNode.getHost(), useDbNode.getPort(), useDbNode.getDbName(), 
                useDbNode.getUsername(), useDbNode.getPassword(), tryConnectTimeout);
        openReplicator.setUser(useDbNode.getUsername());
        openReplicator.setPassword(useDbNode.getPassword());
        openReplicator.setHost(useDbNode.getHost());
        openReplicator.setPort(useDbNode.getPort());
        
        //获取master生成的binlog的信息
        BinlogMasterStatus binlogMasterStatus = DbMetadata.getBinlongMasterStatus();
        openReplicator.setBinlogFileName(binlogMasterStatus.getBinlogName());
        if(PositionKeeper.getPosition() == 0l || reflashPosition) { 
            //如果postion是一个初始化的状态,那么直接用从数据库中获取到的position
            openReplicator.setBinlogPosition(binlogMasterStatus.getPosition());
        } else { //否则使用zookeeper上的position
            openReplicator.setBinlogPosition(PositionKeeper.getPosition());
        }
        
        openReplicator.setBinlogEventListener(listener);
        binlogFileName = openReplicator.getBinlogFileName();
    }
    
    /**
     * 注册自己到zookeeper上面
     * @param path
     * @param child
     * @throws UnknownHostException 
     */
    private void registToZookeeper(String path) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(path).append(Consts.ZK_PATH_SEPARATOR).append(Consts.DATACANAL_TASK_INSTANCE);
        //该分片正在运行的instance,一个分片无需多个intance抽取
        //多个instance抽取,数据就会重复
        List<String> runningInstances = zkClient.getChildren(pathBuilder.toString());
        if(0!=runningInstances.size()) {
            LOG.warn("Instance is running, no need start duplicated. running node {}, JVM will exit.", runningInstances.get(0));
            System.exit(0);
        }
        
        ZkUtil.createChildPath(zkClient, pathBuilder.toString(), String.valueOf(nodeId), Status.RUNNING, CreateMode.EPHEMERAL);
        //捕获该instance的status的变化
        //center可能更新该值,让instance停止
        pathBuilder.append(Consts.ZK_PATH_SEPARATOR).append(nodeId);
        zkClient.subscribeDataChanges(pathBuilder.toString(), new StatusListener(positionSyncZkPeriod));
    }
    
    /**
     * 构造目标数据库的datasource
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
