package com.beanpattern.model;

/**
 * 手机号未绑定异常：用于拦截需要手机号绑定的业务操作。
 */
public class PhoneUnboundException extends RuntimeException {
    public PhoneUnboundException() {
        super("请先绑定手机号");
    }

    public PhoneUnboundException(String message) {
        super(message);
    }
}
