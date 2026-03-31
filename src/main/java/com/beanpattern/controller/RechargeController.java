package com.beanpattern.controller;

import com.beanpattern.entity.RechargePlanEntity;
import com.beanpattern.mapper.RechargePlanMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.RechargePlanVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 充值套餐接口
 * GET /api/recharge/plans - 获取上线中的套餐列表（小程序VIP页使用）
 */
@RestController
@RequestMapping("/api/recharge")
public class RechargeController {

    private final RechargePlanMapper rechargePlanMapper;

    public RechargeController(RechargePlanMapper rechargePlanMapper) {
        this.rechargePlanMapper = rechargePlanMapper;
    }

    @GetMapping("/plans")
    public ApiResponse<List<RechargePlanVO>> plans() {
        return ApiResponse.ok(
                rechargePlanMapper.listActive().stream()
                        .map(RechargePlanVO::from)
                        .collect(Collectors.toList())
        );
    }
}
