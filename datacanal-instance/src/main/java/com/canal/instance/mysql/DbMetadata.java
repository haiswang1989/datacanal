package com.canal.instance.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.datacanal.common.model.BinlogInfo;
import com.datacanal.common.model.BinlogMasterStatus;
import com.datacanal.common.model.ColumnInfo;

import lombok.Setter;

/**
 * 目标库的元数据信息
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月25日 下午6:32:41
 */
public class DbMetadata {
    
    public static final Logger LOG = LoggerFactory.getLogger(DbMetadata.class);
    
    @Setter
    private static JdbcTemplate jdbcTemplate;
    
    /**
     * 获取一个库中表与表字段的映射关系
     * @return
     */
    public static Map<String, List<ColumnInfo>> getColumns() {
        Map<String, List<ColumnInfo>> columns = new HashMap<>();
        Connection conn = null;
        try {
            conn = jdbcTemplate.getDataSource().getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet databaseRs = metaData.getCatalogs();
            String[] tableType = {"TABLE"};
            while(databaseRs.next()) {
                String databaseName = databaseRs.getString("TABLE_CAT");
                ResultSet tableRs = metaData.getTables(databaseName, null, null, tableType);
                while(tableRs.next()) {
                    String tableName = tableRs.getString("TABLE_NAME");
                    String key = databaseName + "." + tableName;
                    ResultSet columnRs = metaData.getColumns(databaseName, null, tableName, null);
                    
                    columns.put(key, new ArrayList<ColumnInfo>());
                    while(columnRs.next()) {
                        ColumnInfo columnInfo = new ColumnInfo(columnRs.getString("COLUMN_NAME"), columnRs.getString("TYPE_NAME"));
                        columns.get(key).add(columnInfo);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        
        return columns;
    }
    
    /**
     * 获取该库的binlog文件的信息
     * mysql> show binary logs
     * 
     * +------------------+-----------+
     * | Log_name         | File_size |
     * +------------------+-----------+
     * | mysql-bin.000001 |       126 |
     * | mysql-bin.000002 |       126 |
     * | mysql-bin.000003 |      6819 |
     * | mysql-bin.000004 |      1868 |
     * +------------------+-----------+
     * @return
     */
    public static List<BinlogInfo> getBinlongInfo() {
        List<BinlogInfo> binlogList = new ArrayList<>();
        List<Map<String, Object>> results = jdbcTemplate.queryForList("show binary logs");
        for (Map<String, Object> result : results) {
            binlogList.add(new BinlogInfo(String.valueOf(result.get("Log_name")), Long.parseLong(result.get("File_size") + "")));
        }
        
        return binlogList;
    }
    
    /**
     * 获取"活跃"binlog的状态
     * mysql> show master status;
     * 
     * +------------------+----------+--------------+------------------+
     * | File             | Position | Binlog_Do_DB | Binlog_Ignore_DB |
     * +------------------+----------+--------------+------------------+
     * | mysql-bin.000004 |     1868 |              |                  |
     * +------------------+----------+--------------+------------------+
     * @return
     */
    public static BinlogMasterStatus getBinlongMasterStatus() {
        BinlogMasterStatus binlogMasterStatus = new BinlogMasterStatus();
        Map<String, Object> result = jdbcTemplate.queryForMap("show master status");
        binlogMasterStatus.setBinlogName(String.valueOf(result.get("File")));
        binlogMasterStatus.setPosition(Long.valueOf(result.get("Position") + ""));
        return binlogMasterStatus;
    }
    
    /**
     * 获取open-replicator所连接的mysql服务器的serverid信息
     * @return
     */
    public static int getServerId() {
        Map<String, Object> result = jdbcTemplate.queryForMap("show variables like 'server_id'");
        return Integer.parseInt(result.get("Value") + "");
    }
}
