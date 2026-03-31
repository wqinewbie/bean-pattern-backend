package com.beanpattern.model;

/**
 * 未登录异常，由 GlobalExceptionHandler 统一返回 HTTP 401
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("未登录或登录已过期");
    }
    public UnauthorizedException(String message) {
        super(message);
    }
}
