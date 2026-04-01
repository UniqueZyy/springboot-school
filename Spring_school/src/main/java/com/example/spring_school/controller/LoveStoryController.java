package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.DonationDistribution;
import com.example.spring_school.entity.DonationType;
import com.example.spring_school.service.DonationDistributionService;
import com.example.spring_school.service.DonationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 爱心事迹控制器
 */
@RestController
@RequestMapping("/api/love-stories")
public class LoveStoryController {

    @Autowired
    private DonationDistributionService donationDistributionService;

    @Autowired
    private DonationTypeService donationTypeService;

    /**
     * 获取爱心事迹列表
     */
    @GetMapping
    public List<Map<String, Object>> getLoveStories(@RequestParam(required = false) String keyword) {
        QueryWrapper<DonationDistribution> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("distribution_object", keyword)
                    .or().like("reason", keyword);
        }
        queryWrapper.orderByDesc("distribution_time");
        
        List<DonationDistribution> distributions = donationDistributionService.list(queryWrapper);
        
        // 构建返回结果
        return distributions.stream().map(distribution -> {
            Map<String, Object> story = new HashMap<>();
            story.put("id", distribution.getId());
            
            // 获取物资类型名称
            String typeName = "未知类型";
            if (distribution.getTypeId() != null) {
                DonationType donationType = donationTypeService.getById(distribution.getTypeId());
                if (donationType != null) {
                    typeName = donationType.getTypeName();
                }
            }
            
            story.put("title", typeName + "分配处理情况");
            story.put("description", "分配给：" + distribution.getDistributionObject());
            story.put("time", distribution.getDistributionTime());
            story.put("details", "分配类型：" + typeName + "\n" +
                    "分配数量：" + distribution.getQuantity() + "件\n" +
                    "分配对象：" + distribution.getDistributionObject() + "\n" +
                    "分配原因：" + distribution.getReason() + "\n" +
                    "分配时间：" + distribution.getDistributionTime());
            return story;
        }).collect(Collectors.toList());
    }

    /**
     * 分页获取爱心事迹
     */
    @GetMapping("/page")
    public Page<Map<String, Object>> getLoveStoriesPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<DonationDistribution> page = new Page<>(current, size);
        QueryWrapper<DonationDistribution> queryWrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("distribution_object", keyword)
                    .or().like("reason", keyword);
        }
        queryWrapper.orderByDesc("distribution_time");
        
        Page<DonationDistribution> distributionPage = donationDistributionService.page(page, queryWrapper);
        
        // 转换为自定义格式
        List<Map<String, Object>> stories = distributionPage.getRecords().stream().map(distribution -> {
            Map<String, Object> story = new HashMap<>();
            story.put("id", distribution.getId());
            
            // 获取物资类型名称
            String typeName = "未知类型";
            if (distribution.getTypeId() != null) {
                DonationType donationType = donationTypeService.getById(distribution.getTypeId());
                if (donationType != null) {
                    typeName = donationType.getTypeName();
                }
            }
            
            story.put("title", typeName + "分配处理情况");
            story.put("description", "分配给：" + distribution.getDistributionObject());
            story.put("time", distribution.getDistributionTime());
            story.put("details", "分配类型：" + typeName + "\n" +
                    "分配数量：" + distribution.getQuantity() + "件\n" +
                    "分配对象：" + distribution.getDistributionObject() + "\n" +
                    "分配原因：" + distribution.getReason() + "\n" +
                    "分配时间：" + distribution.getDistributionTime());
            return story;
        }).collect(Collectors.toList());
        
        Page<Map<String, Object>> resultPage = new Page<>(current, size);
        resultPage.setRecords(stories);
        resultPage.setTotal(distributionPage.getTotal());
        resultPage.setSize(distributionPage.getSize());
        resultPage.setCurrent(distributionPage.getCurrent());
        resultPage.setPages(distributionPage.getPages());
        
        return resultPage;
    }
}