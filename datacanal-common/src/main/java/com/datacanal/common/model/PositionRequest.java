package com.datacanal.common.model;

import lombok.Getter;

public enum PositionRequest {
    
    EARLIEST("最开始的位置"),LATEST("最后的位置");
    
    @Getter
    private String desc;
    
    private PositionRequest(String descArg) {
        this.desc = descArg;
    }
}
