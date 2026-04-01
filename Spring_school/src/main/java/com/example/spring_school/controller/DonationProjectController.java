package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.DonationProject;
import com.example.spring_school.service.DonationProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 捐赠项目表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@RestController
@RequestMapping("/api/donation-projects")
public class DonationProjectController {

    @Autowired
    private DonationProjectService donationProjectService;

    /**
     * 获取所有捐赠项目
     */
    @GetMapping
    public Object getAllDonationProjects() {
        return donationProjectService.list();
    }

    /**
     * 分页获取捐赠项目
     */
    @GetMapping("/page")
    public Page<DonationProject> pageDonationProjects(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<DonationProject> page = new Page<>(current, size);
        QueryWrapper<DonationProject> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("project_name", keyword).or().like("description", keyword);
        }
        if (status != null && !status.trim().isEmpty()) {
            try {
                Integer statusValue = Integer.parseInt(status);
                queryWrapper.eq("status", statusValue);
            } catch (NumberFormatException ignored) {}
        }
        return donationProjectService.page(page, queryWrapper);
    }

    /**
     * 添加捐赠项目
     */
    @PostMapping
    public boolean createDonationProject(@RequestBody DonationProject donationProject) {
        donationProject.setCreateTime(LocalDateTime.now());
        return donationProjectService.save(donationProject);
    }

    /**
     * 更新捐赠项目
     */
    @PutMapping("/{id}")
    public boolean updateDonationProject(@PathVariable Long id, @RequestBody DonationProject donationProject) {
        donationProject.setId(id);
        return donationProjectService.updateById(donationProject);
    }

    /**
     * 删除捐赠项目
     */
    @DeleteMapping("/{id}")
    public boolean deleteDonationProject(@PathVariable Long id) {
        return donationProjectService.removeById(id);
    }
}