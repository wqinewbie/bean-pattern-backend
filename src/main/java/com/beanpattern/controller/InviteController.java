package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.InviteCodeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invite")
public class InviteController {

    private final SessionHelper sessionHelper;
    private final InviteCodeService inviteCodeService;

    public InviteController(SessionHelper sessionHelper, InviteCodeService inviteCodeService) {
        this.sessionHelper = sessionHelper;
        this.inviteCodeService = inviteCodeService;
    }

    @GetMapping("/my-code")
    public ApiResponse<Map<String, Object>> myCode(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        String inviteCode = inviteCodeService.ensureInviteCode(user);
        return ApiResponse.ok(Map.of("inviteCode", inviteCode));
    }

    @PostMapping("/bind")
    public ApiResponse<Map<String, Object>> bind(HttpServletRequest request, @RequestBody Map<String, String> body) {
        UserEntity user = sessionHelper.requireUser(request);
        String inviteCode = body == null ? "" : body.getOrDefault("inviteCode", "");
        inviteCodeService.bindInviteRelation(user.getId(), inviteCode);
        return ApiResponse.ok(Map.of("success", true));
    }

    @GetMapping("/records")
    public ApiResponse<Map<String, Object>> records(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        String inviteCode = inviteCodeService.ensureInviteCode(user);
        List<Map<String, Object>> records = inviteCodeService.listInvites(user.getId()).stream().map(item -> Map.<String, Object>of(
                "id", item.getId(),
                "inviteeUserId", item.getInviteeUserId(),
                "nickName", item.getInviteeNickName() == null ? "新朋友" : item.getInviteeNickName(),
                "avatarUrl", item.getInviteeAvatarUrl() == null ? "" : item.getInviteeAvatarUrl(),
                "status", item.getStatus() == null ? 1 : item.getStatus(),
                "firstPaidAt", item.getFirstPaidAt() == null ? "" : item.getFirstPaidAt().toString(),
                "createdAt", item.getCreatedAt() == null ? "" : item.getCreatedAt().toString()
        )).toList();
        return ApiResponse.ok(Map.of(
                "inviteCode", inviteCode,
                "registeredCount", inviteCodeService.countInvitedRegistered(user.getId()),
                "paidCount", inviteCodeService.countInvitedPaid(user.getId()),
                "records", records
        ));
    }
}
