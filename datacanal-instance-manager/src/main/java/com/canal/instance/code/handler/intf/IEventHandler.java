package com.canal.instance.code.handler.intf;

import com.google.code.or.binlog.BinlogEventV4;

/**
 * 数据库事件处理接口
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:34:18
 */
public interface IEventHandler {
    
    public void handle(BinlogEventV4 event);
    
}
