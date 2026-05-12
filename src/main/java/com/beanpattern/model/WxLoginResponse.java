package com.beanpattern.model;

/**
 * 微信登录响应。
 * sessionId 与 token 指向同一个值，sessionId 供小程序端使用。
 */
public class WxLoginResponse {

    private String token;
    private String openId;
    private String inviteCode;

    public WxLoginResponse() {
    }

    public WxLoginResponse(String token, String openId, String inviteCode) {
        this.token = token;
        this.openId = openId;
        this.inviteCode = inviteCode;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    /** 别名：与 token 相同，供小程序端 data.sessionId 使用 */
    public String getSessionId() { return token; }

    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}
