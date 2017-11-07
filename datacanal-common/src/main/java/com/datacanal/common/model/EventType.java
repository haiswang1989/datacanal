package com.datacanal.common.model;

import lombok.Getter;

public enum EventType {
    
    INSTANCE_START(1,"服务端向该客户端发起启动instance请求"),
    HEART_BEAT(2,"心跳");
    
    @Getter
    private int value;
    
    @Getter
    private String desc;
    
    private EventType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
