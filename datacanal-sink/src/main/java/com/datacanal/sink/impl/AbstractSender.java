package com.datacanal.sink.impl;

import com.datacanal.sink.intf.ISender;

/**
 * 消息发送的抽象类
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午12:15:04
 */
public abstract class AbstractSender implements ISender {
    
    @Override
    public void send() {
    }
    
    @Override
    public void sendKafka(String topic, byte[] message) {
    }
    
}
