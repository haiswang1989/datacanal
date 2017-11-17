package com.datacanal.common.model;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.BinlogEventV4Header;
import com.google.code.or.binlog.impl.event.AbstractBinlogEventV4;
import lombok.Data;

/**
 * 
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月23日 下午3:51:26
 */
@Data
public class CDCEvent {
    
    private long eventId = 0;//事件唯一标识
    private String databaseName = null;
    private String tableName = null;
    private int eventType = 0;//事件类型
    private long timestamp = 0;//事件发生的时间戳[MySQL服务器的时间]
    private long timestampReceipt = 0;//Open-replicator接收到的时间戳[CDC执行的时间戳]
    private String binlogName = null;// binlog file name
    private long position = 0;
    private long nextPostion = 0;
    private long serverId = 0;
    private Map<String,String> before = null;
    private Map<String,String> after = null;
    private Boolean isDdl= null;
    private String sql = null;
    
    private static AtomicLong uuid = new AtomicLong(0);
    public CDCEvent(){}

    public CDCEvent(final AbstractBinlogEventV4 are, String databaseName, String tableName, String binlogName){
        this.init(are);
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.binlogName = binlogName;
    }

    private void init(final BinlogEventV4 be){
        this.eventId = uuid.getAndAdd(1);
        BinlogEventV4Header header = be.getHeader();
        this.timestamp = header.getTimestamp();
        this.eventType = header.getEventType();
        this.serverId = header.getServerId();
        this.timestampReceipt = header.getTimestampOfReceipt();
        this.position = header.getPosition();
        this.nextPostion = header.getNextPosition();
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("{ eventId:").append(eventId);
        builder.append(",databaseName:").append(databaseName);
        builder.append(",tableName:").append(tableName);
        builder.append(",eventType:").append(eventType);
        builder.append(",timestamp:").append(timestamp);
        builder.append(",timestampReceipt:").append(timestampReceipt);
        builder.append(",binlogName:").append(binlogName);
        builder.append(",position:").append(position);
        builder.append(",nextPostion:").append(nextPostion);
        builder.append(",serverId:").append(serverId);
        builder.append(",isDdl:").append(isDdl);
        builder.append(",sql:").append(sql);
        builder.append(",before:").append(before);
        builder.append(",after:").append(after).append("}");

        return builder.toString();
    }
}
