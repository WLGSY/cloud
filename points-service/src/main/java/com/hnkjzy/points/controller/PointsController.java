package com.hnkjzy.points.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hnkjzy.points.common.PageResult;
import com.hnkjzy.points.common.Result;
import com.hnkjzy.points.config.PointsConfig;
import com.hnkjzy.points.entity.PointsLog;
import com.hnkjzy.points.mapper.PointsLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/points")
public class PointsController {

    @Autowired
    private PointsLogMapper pointsLogMapper;

    @Autowired
    private PointsConfig pointsConfig;

    /**
     * 获取我的积分（总积分 + 积分配置）
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyPoints(@RequestHeader(value = "X-User-Id", required = false) Long xUserId) {
        if (xUserId == null) {
            log.warn("[积分服务] X-User-Id header 为空");
            return Result.error("未获取到用户信息");
        }

        // 查询用户总积分
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, xUserId);
        java.util.List<PointsLog> logs = pointsLogMapper.selectList(wrapper);

        int totalPoints = logs.stream().mapToInt(PointsLog::getPoints).sum();

        Map<String, Object> data = new HashMap<>();
        data.put("totalPoints", totalPoints);
        data.put("rate", pointsConfig.getRate());
        data.put("enabled", pointsConfig.getEnabled());
        data.put("maxPerOrder", pointsConfig.getMaxPerOrder());

        log.info("[积分服务] 查询用户积分，userId: {}, totalPoints: {}", xUserId, totalPoints);
        return Result.success(data);
    }

    /**
     * 获取积分明细（分页）
     */
    @GetMapping("/log")
    public Result<PageResult<PointsLog>> getLog(
            @RequestHeader(value = "X-User-Id", required = false) Long xUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        if (xUserId == null) {
            log.warn("[积分服务] X-User-Id header 为空");
            return Result.error("未获取到用户信息");
        }

        Page<PointsLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, xUserId)
                .orderByDesc(PointsLog::getCreateTime);

        Page<PointsLog> resultPage = pointsLogMapper.selectPage(page, wrapper);

        PageResult<PointsLog> pageResult = new PageResult<>(resultPage.getRecords(), resultPage.getTotal());
        return Result.success(pageResult);
    }
}
