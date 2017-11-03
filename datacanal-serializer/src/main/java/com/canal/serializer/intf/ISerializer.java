package com.canal.serializer.intf;

/**
 * 序列化接口
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年10月24日 下午12:19:43
 */
public interface ISerializer<T> {
    
    public byte[] encode(T event);
    
    public T decode(byte[] b, Class<T> clazz);
    
}
