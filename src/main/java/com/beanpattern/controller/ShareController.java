package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.ShareRecord;
import com.beanpattern.entity.ShareVisitor;
import com.beanpattern.entity.UserGift;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.ShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 分享 Controller
 */
@RestController
@RequestMapping("/api/share")
public class ShareController {

    private final SessionHelper sessionHelper;
    private final ShareService shareService;

    public ShareController(SessionHelper sessionHelper, ShareService shareService) {
        this.sessionHelper = sessionHelper;
        this.shareService = shareService;
    }

    /**
     * 创建分享记录
     */
    @PostMapping("/create")
    public ApiResponse<ShareRecord> createShare(@RequestBody Map<String, String> body,
                                               HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        String shareScene = body.get("shareScene");
        String targetId = body.get("targetId");
        String shareTicket = body.get("shareTicket");

        ShareRecord record = shareService.createShareRecord(user.getId(), shareScene, targetId, shareTicket);
        return ApiResponse.ok(record);
    }

    /**
     * 获取用户分享记录
     */
    @GetMapping("/records")
    public ApiResponse<List<ShareRecord>> getShareRecords(HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<ShareRecord> records = shareService.getUserShareRecords(user.getId());
        return ApiResponse.ok(records);
    }

    /**
     * 获取分享记录详情
     */
    @GetMapping("/records/{recordId}")
    public ApiResponse<Map<String, Object>> getShareRecordDetail(@PathVariable Long recordId,
                                                                  HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        
        // 从数据库获取分享记录
        ShareRecord record = shareService.getShareRecordById(recordId);
        if (record == null) {
            return ApiResponse.fail("分享记录不存在");
        }
        
        List<ShareVisitor> visitors = shareService.getShareVisitors(recordId);
        int rewardStatus = shareService.getRewardStatus(recordId);
        
        return ApiResponse.ok(Map.of(
                "record", record,
                "visitors", visitors,
                "rewardStatus", rewardStatus
        ));
    }

    /**
     * 记录分享访问（由前端调用）
     */
    @PostMapping("/visit")
    public ApiResponse<String> recordVisit(@RequestBody Map<String, Object> body) {
        Long shareRecordId = body.get("shareRecordId") != null ? 
                Long.valueOf(body.get("shareRecordId").toString()) : null;
        Long shareUserId = body.get("shareUserId") != null ? 
                Long.valueOf(body.get("shareUserId").toString()) : null;
        String visitorOpenid = body.get("visitorOpenid") != null ? 
                body.get("visitorOpenid").toString() : null;
        Boolean isNewUser = body.get("isNewUser") != null ? 
                Boolean.valueOf(body.get("isNewUser").toString()) : false;

        if (shareRecordId == null || visitorOpenid == null) {
            return ApiResponse.fail("参数不完整");
        }

        shareService.recordVisitor(shareRecordId, shareUserId, visitorOpenid, isNewUser);
        return ApiResponse.ok("ok");
    }

    /**
     * 领取分享奖励
     */
    @PostMapping("/claim")
    public ApiResponse<UserGift> claimReward(@RequestBody Map<String, Long> body,
                                             HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        Long recordId = body.get("recordId");
        if (recordId == null) {
            return ApiResponse.fail("记录ID不能为空");
        }

        UserGift gift = shareService.claimShareReward(user.getId(), recordId);
        return ApiResponse.ok(gift);
    }
}
