package com.canal.instance.code.handler.impl;

import org.springframework.stereotype.Component;

import com.google.code.or.binlog.BinlogEventV4;

/**
 * 对FORMAT_DESCRIPTION_EVENT,QUERY_EVENT,XID_EVENT事件的处理
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 上午10:45:08
 */
@Component
public class EmptyEventHandler extends AbstractEventHandler {

    @Override
    public void handle(BinlogEventV4 event) {
    }

}
