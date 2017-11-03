package com.canal.serializer.impl;

import com.alibaba.fastjson.JSON;

/**
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月2日 下午4:58:56
 * @param <T>
 */
public class JSONSerializer<T> extends AbstractSerializer<T> {

    @Override
    public byte[] encode(T event) {
        String jsonString = JSON.toJSONString(event);
        return jsonString.getBytes();
    }

    @Override
    public T decode(byte[] b, Class<T> clazz) {
        String jsonString = new String(b);
        return JSON.parseObject(jsonString, clazz);
    }
}
