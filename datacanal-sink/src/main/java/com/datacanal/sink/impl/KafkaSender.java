package com.datacanal.sink.impl;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.Setter;

/**
 * 将消息发送到Kafka
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午12:15:37
 */
public class KafkaSender extends AbstractSender {
    
    public static final Logger LOG = LoggerFactory.getLogger(KafkaSender.class);
    
    @Setter
    private Properties prop;
    
    private Producer<String, byte[]> producer;
    
    public void init() {
        ProducerConfig producerConfig = new ProducerConfig(prop);
        producer = new Producer<>(producerConfig);
    }
    
    @Override
    public void sendKafka(String topic, byte[] message) {
        LOG.info("Topic : {} , body : {} ", topic, new String(message));
        producer.send(new KeyedMessage<String, byte[]>(topic, message));
    }
}
