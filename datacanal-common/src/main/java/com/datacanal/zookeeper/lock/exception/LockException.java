package com.datacanal.zookeeper.lock.exception;

/**
 * 获取锁异常
 * <p>Description:</p>
 * @author hansen.wang
 * @date 2017年11月1日 上午9:37:58
 */
public class LockException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public LockException() {
        super();
    }
    
    public LockException(String message) {
        super(message);
    }
    
    public LockException(Throwable throwable) {
        super(throwable);
    }
    
    public LockException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
