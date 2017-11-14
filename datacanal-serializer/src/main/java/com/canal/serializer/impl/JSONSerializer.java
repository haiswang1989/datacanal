package com.canal.serializer.impl;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

/**
 * JSON进行编解码
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 下午4:58:56
 * @param <T>
 */
@Component("jsonSerializer")
public class JSONSerializer<T> extends AbstractSerializer<T> {

    @Override
    public byte[] encode(T event) {
        return JSON.toJSONBytes(event);
    }

    @Override
    public T decode(byte[] b, Class<T> clazz) {
        return JSON.parseObject(b, clazz);
    }
}
