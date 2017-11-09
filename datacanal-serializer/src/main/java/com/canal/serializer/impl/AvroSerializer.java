package com.canal.serializer.impl;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

/**
 * 采用Avro序列化框架进行序列化与反序列化
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午12:26:09
 */
@Component
public class AvroSerializer<T> extends AbstractSerializer<T> {

    @Override
    public byte[] encode(T event) {
        //TODO
        String jsonString = JSON.toJSONString(event);
        return jsonString.getBytes();
    }

    @Override
    public T decode(byte[] b, Class<T> clazz) {
        //TODO 
        String jsonString = new String(b);
        return JSON.parseObject(jsonString, clazz);
    }
}
