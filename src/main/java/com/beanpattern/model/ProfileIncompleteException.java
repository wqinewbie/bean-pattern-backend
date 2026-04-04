package com.beanpattern.model;

/**
 * 资料未完善异常：用于拦截需要完整资料的业务操作。
 */
public class ProfileIncompleteException extends RuntimeException {
    public ProfileIncompleteException() {
        super("请先完善昵称和头像");
    }

    public ProfileIncompleteException(String message) {
        super(message);
    }
}
