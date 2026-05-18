package com.beanpattern.model;

/**
 * 统一错误码枚举。
 *
 * 分段规则：
 * 1xxxx 用户/认证
 * 2xxxx 订单/支付
 * 3xxxx AI/图片处理
 * 4xxxx 礼品/任务/活动
 * 5xxxx 资源/存储
 * 9xxxx 系统通用
 */
public enum ErrorCode {

    // ---- 用户/认证 1xxxx ----
    UNAUTHORIZED(10001, "未登录或登录已过期"),
    PROFILE_INCOMPLETE(10010, "请先完善个人资料"),
    USER_NOT_FOUND(10011, "用户不存在"),
    USER_DISABLED(10012, "账号已被禁用"),
    PHONE_INVALID(10013, "手机号格式不正确"),

    // ---- 订单/支付 2xxxx ----
    ORDER_NOT_FOUND(20001, "订单不存在"),
    ORDER_STATUS_INVALID(20002, "订单状态不允许此操作"),
    PAYMENT_FAILED(20003, "支付失败"),
    PACKAGE_NOT_FOUND(20004, "套餐不存在"),
    PACKAGE_CODE_INVALID(20005, "套餐代码不能为空"),

    // ---- AI/图片 3xxxx ----
    AI_QUOTA_EXCEEDED(40001, "AI次数已用完"),
    AI_TASK_NOT_FOUND(40002, "AI任务不存在"),
    AI_GENERATE_FAILED(40003, "AI生成失败"),
    IMAGE_UPLOAD_FAILED(40004, "图片上传失败"),

    // ---- 礼品/任务/活动 4xxxx ----
    GIFT_NOT_FOUND(50001, "礼品不存在"),
    GIFT_ALREADY_USED(50002, "礼品已使用或不可用"),
    GIFT_EXPIRED(50003, "礼品已过期"),
    GIFT_PACKAGE_NOT_FOUND(50004, "礼品包不存在或未启用"),
    TASK_NOT_FOUND(50005, "任务不存在"),
    TASK_ALREADY_CLAIMED(50006, "任务奖励已领取"),
    ACTIVITY_NOT_FOUND(50007, "活动不存在"),
    ACTIVITY_NOT_STARTED(50008, "活动未开始"),
    ACTIVITY_ENDED(50009, "活动已结束"),
    ACTIVITY_QUOTA_EXHAUSTED(50010, "活动名额已抢完"),
    CHECKIN_ALREADY_DONE(50011, "今天已经签到过了"),
    CHECKIN_NOT_ACTIVE(50012, "签到暂未开启"),

    // ---- 资源/存储 5xxxx ----
    STORAGE_QUOTA_EXCEEDED(60001, "存储空间不足"),
    DRAFT_QUOTA_EXCEEDED(60002, "草稿数量已达上限"),

    // ---- 系统通用 9xxxx ----
    INTERNAL_ERROR(90001, "服务器内部错误，请稍后重试"),
    PARAM_INVALID(90002, "请求参数有误"),
    ;

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }

    /** 构造带自定义消息的失败响应 */
    public <T> ApiResponse<T> fail(String customMessage) {
        return ApiResponse.fail(code, customMessage != null ? customMessage : defaultMessage);
    }

    /** 构造默认消息的失败响应 */
    public <T> ApiResponse<T> fail() {
        return ApiResponse.fail(code, defaultMessage);
    }
}
