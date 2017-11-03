package com.datacanal.common.model;

import lombok.Getter;
import lombok.Setter;

/**
 * server-client通信的消息封装
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月3日 上午10:36:56
 */
@Getter
@Setter
public class Command {
    
    private EventType eventType;
    
    private Object obj;
}
