package com.datacanal.common.model;

import lombok.Getter;

public enum EventType {
    
    SYNC(1,"客户端向服务端同步运行在当前结点的instance"), 
    INSTANCE_START(2,"服务端向该客户端发起启动instance请求"),
    HEART_BEAT(3,"心跳");
    
    @Getter
    private int value;
    
    @Getter
    private String desc;
    
    private EventType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
