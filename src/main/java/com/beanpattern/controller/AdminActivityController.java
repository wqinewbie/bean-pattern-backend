package com.beanpattern.controller;

import com.beanpattern.entity.ActivityConfig;
import com.beanpattern.mapper.ActivityConfigMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动中心管理 Controller（后台管理）
 */
@RestController
@RequestMapping("/admin/activities")
public class AdminActivityController {

    private final ActivityConfigMapper activityMapper;

    public AdminActivityController(ActivityConfigMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    /**
     * 获取活动列表
     */
    @GetMapping
    public ApiResponse<List<ActivityConfig>> getActivities() {
        List<ActivityConfig> activities = activityMapper.findAll();
        return ApiResponse.ok(activities);
    }

    /**
     * 创建活动
     */
    @PostMapping
    public ApiResponse<String> createActivity(@RequestBody ActivityConfig activity) {
        try {
            // 设置默认值
            if (activity.getRemainQuota() == null) {
                activity.setRemainQuota(activity.getTotalQuota());
            }
            if (activity.getStatus() == null) {
                activity.setStatus(true);
            }

            activityMapper.insert(activity);
            return ApiResponse.ok("创建成功");
        } catch (Exception e) {
            return ApiResponse.fail("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public ApiResponse<String> updateActivity(@PathVariable Long id, @RequestBody ActivityConfig activity) {
        try {
            activity.setId(id);
            activityMapper.update(activity);
            return ApiResponse.ok("更新成功");
        } catch (Exception e) {
            return ApiResponse.fail("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteActivity(@PathVariable Long id) {
        try {
            activityMapper.deleteById(id);
            return ApiResponse.ok("删除成功");
        } catch (Exception e) {
            return ApiResponse.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 上线/下线活动
     */
    @PutMapping("/{id}/status")
    public ApiResponse<String> toggleStatus(@PathVariable Long id, @RequestParam Boolean status) {
        try {
            activityMapper.updateStatus(id, status);
            return ApiResponse.ok("状态更新成功");
        } catch (Exception e) {
            return ApiResponse.fail("状态更新失败：" + e.getMessage());
        }
    }
}
