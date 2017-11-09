package com.canal.instance.exception;

/**
 * 启动参数异常
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月9日 下午5:27:47
 */
public class ParamException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public ParamException(String message) {
        super(message);
    }
    
    public ParamException(String message, Exception e) {
        super(message, e);
    }
}
