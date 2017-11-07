package com.datacanal.sink.intf;

/**
 * 消息的发送接口
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午12:13:36
 */
public interface ISender {
    
    public void send(String topic, byte[] message);
    
}
