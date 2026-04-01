package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.DonationRecord;
import com.example.spring_school.service.DonationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 捐赠记录表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@RestController
@RequestMapping("/api/donation-records")
public class DonationRecordController {

    @Autowired
    private DonationRecordService donationRecordService;

    /**
     * 获取所有捐赠记录
     */
    @GetMapping
    public Object getAllDonationRecords() {
        return donationRecordService.list();
    }

    /**
     * 分页获取捐赠记录
     */
    @GetMapping("/page")
    public Page<DonationRecord> pageDonationRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<DonationRecord> page = new Page<>(current, size);
        QueryWrapper<DonationRecord> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("remark", keyword);
        }
        return donationRecordService.page(page, queryWrapper);
    }

    /**
     * 添加捐赠记录
     */
    @PostMapping
    public boolean createDonationRecord(@RequestBody DonationRecord donationRecord) {
        donationRecord.setDonationTime(LocalDateTime.now());
        return donationRecordService.save(donationRecord);
    }

    /**
     * 更新捐赠记录
     */
    @PutMapping("/{id}")
    public boolean updateDonationRecord(@PathVariable Long id, @RequestBody DonationRecord donationRecord) {
        donationRecord.setId(id);
        return donationRecordService.updateById(donationRecord);
    }

    /**
     * 删除捐赠记录
     */
    @DeleteMapping("/{id}")
    public boolean deleteDonationRecord(@PathVariable Long id) {
        return donationRecordService.removeById(id);
    }
}