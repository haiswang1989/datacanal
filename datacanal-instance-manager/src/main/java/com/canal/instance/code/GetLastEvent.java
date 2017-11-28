package com.canal.instance.code;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GetLastEvent {

    public static void main(String[] args) throws Exception {
        
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.56.101:3306/", "root", "123456");
        
        String showMasterStatusSql = "show master status";
        String showBinlogEventSql = "show binlog events in '%s'";
        
        Statement stmt = conn.createStatement();
        
        ResultSet rs = stmt.executeQuery(showMasterStatusSql);
        String binlogName = null;
        if(rs.next()) {
            binlogName = rs.getString("File");
        }
        
        System.out.println("binlog name : " + binlogName);
        
        rs.close();
        
        rs = stmt.executeQuery(String.format(showBinlogEventSql, binlogName));
        List<Long> endLogPosList = new ArrayList<>();
        while(rs.next()) {
            endLogPosList.add(rs.getLong("End_log_pos"));
        }
        
        
        rs.close();
        stmt.close();
        conn.close();
    }

}
