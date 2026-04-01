package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.DonationDistribution;
import com.example.spring_school.service.DonationDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 捐赠分配表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@RestController
@RequestMapping("/api/donation-distributions")
public class DonationDistributionController {

    @Autowired
    private DonationDistributionService donationDistributionService;

    /**
     * 分页获取捐赠分配
     */
    @GetMapping("/page")
    public Page<DonationDistribution> pageDonationDistributions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String typeId) {
        Page<DonationDistribution> page = new Page<>(current, size);
        QueryWrapper<DonationDistribution> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("distribution_object", keyword).or().like("reason", keyword);
        }
        if (typeId != null && !typeId.trim().isEmpty()) {
            try {
                Long typeIdValue = Long.parseLong(typeId);
                queryWrapper.eq("type_id", typeIdValue);
            } catch (NumberFormatException ignored) {}
        }
        return donationDistributionService.page(page, queryWrapper);
    }

    /**
     * 添加捐赠分配
     */
    @PostMapping
    public boolean createDonationDistribution(@RequestBody DonationDistribution donationDistribution) {
        donationDistribution.setDistributionTime(LocalDateTime.now());
        return donationDistributionService.save(donationDistribution);
    }

    /**
     * 更新捐赠分配
     */
    @PutMapping("/{id}")
    public boolean updateDonationDistribution(@PathVariable Long id, @RequestBody DonationDistribution donationDistribution) {
        donationDistribution.setId(id);
        return donationDistributionService.updateById(donationDistribution);
    }

    /**
     * 删除捐赠分配
     */
    @DeleteMapping("/{id}")
    public boolean deleteDonationDistribution(@PathVariable Long id) {
        return donationDistributionService.removeById(id);
    }
}