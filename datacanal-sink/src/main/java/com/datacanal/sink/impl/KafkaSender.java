package com.datacanal.sink.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 将消息发送到Kafka
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午12:15:37
 */
@Component
public class KafkaSender extends AbstractSender {
    
    public static final Logger LOG = LoggerFactory.getLogger(KafkaSender.class);
    
    @Override
    public void send(String topic, byte[] message) {
        LOG.info("Topic : {} , body : {} ", topic, new String(message));
    }

}
