package com.datacanal.common.model;

/**
 * Instance运行状态,用于Center控制instance的运行
 * 
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月14日 上午10:59:27
 */
public enum Status {
    
    RUNNING/*刚启动的时候的状态*/, STOP/*停止instance时Center更新该instance的状态*/;
}
