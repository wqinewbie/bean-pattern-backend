package com.beanpattern.model;

import jakarta.validation.constraints.NotBlank;

public class WxLoginRequest {

    @NotBlank(message = "code is required")
    private String code;
    private String inviteCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
