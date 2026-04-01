package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.DonationType;
import com.example.spring_school.service.DonationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 捐赠物资类型表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@RestController
@RequestMapping("/api/donation-types")
public class DonationTypeController {

    @Autowired
    private DonationTypeService donationTypeService;

    /**
     * 获取所有捐赠物资类型
     */
    @GetMapping
    public List<DonationType> getAllDonationTypes() {
        return donationTypeService.list();
    }

    /**
     * 分页获取捐赠物资类型
     */
    @GetMapping("/page")
    public Page<DonationType> pageDonationTypes(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<DonationType> page = new Page<>(current, size);
        QueryWrapper<DonationType> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("type_name", keyword).or().like("description", keyword);
        }
        return donationTypeService.page(page, queryWrapper);
    }

    /**
     * 搜索捐赠物资类型
     */
    @GetMapping("/search")
    public List<DonationType> searchDonationTypes(@RequestParam String keyword) {
        QueryWrapper<DonationType> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("type_name", keyword).or().like("description", keyword);
        }
        return donationTypeService.list(queryWrapper);
    }

    /**
     * 添加捐赠物资类型
     */
    @PostMapping
    public boolean createDonationType(@RequestBody DonationType donationType) {
        return donationTypeService.save(donationType);
    }

    /**
     * 更新捐赠物资类型
     */
    @PutMapping("/{id}")
    public boolean updateDonationType(@PathVariable Long id, @RequestBody DonationType donationType) {
        donationType.setId(id);
        return donationTypeService.updateById(donationType);
    }

    /**
     * 删除捐赠物资类型
     */
    @DeleteMapping("/{id}")
    public boolean deleteDonationType(@PathVariable Long id) {
        return donationTypeService.removeById(id);
    }
}