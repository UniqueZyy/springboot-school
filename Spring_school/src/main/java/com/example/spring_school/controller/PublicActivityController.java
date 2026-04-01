package com.example.spring_school.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.PublicActivity;
import com.example.spring_school.entity.PublicActivityType;
import com.example.spring_school.service.PublicActivityService;
import com.example.spring_school.service.PublicActivityTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
public class PublicActivityController {

    @Autowired
    private PublicActivityService publicActivityService;
    
    @Autowired
    private PublicActivityTypeService publicActivityTypeService;

    @GetMapping
    public List<PublicActivity> getAllActivities() {
        List<PublicActivity> activities = publicActivityService.list();
        Map<Long, String> typeMap = publicActivityTypeService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivityType::getId, PublicActivityType::getTypeName));
        
        // 为每个活动设置类型名称
        for (PublicActivity activity : activities) {
            activity.setTypeName(typeMap.get(activity.getTypeId()));
        }
        
        return activities;
    }

    @GetMapping("/{id}")
    public PublicActivity getActivityById(@PathVariable Long id) {
        try {
            PublicActivity activity = publicActivityService.getById(id);
            if (activity != null) {
                PublicActivityType type = publicActivityTypeService.getById(activity.getTypeId());
                if (type != null) {
                    activity.setTypeName(type.getTypeName());
                }
            }
            return activity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping
    public boolean createActivity(@RequestBody PublicActivity activity) {
        return publicActivityService.save(activity);
    }

    @PutMapping("/{id}")
    public boolean updateActivity(@PathVariable Long id, @RequestBody PublicActivity activity) {
        activity.setId(id);
        return publicActivityService.updateById(activity);
    }

    @DeleteMapping("/{id}")
    public boolean deleteActivity(@PathVariable Long id) {
        return publicActivityService.removeById(id);
    }

    @GetMapping("/page")
    public Page<PublicActivity> pageActivities(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size) {
        Page<PublicActivity> page = new Page<>(current, size);
        Page<PublicActivity> result = publicActivityService.page(page);
        
        // 获取所有活动类型
        Map<Long, String> typeMap = publicActivityTypeService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivityType::getId, PublicActivityType::getTypeName));
        
        // 为每页的活动设置类型名称
        for (PublicActivity activity : result.getRecords()) {
            activity.setTypeName(typeMap.get(activity.getTypeId()));
        }
        
        return result;
    }

    @GetMapping("/type/{typeId}")
    public List<PublicActivity> getActivitiesByType(@PathVariable Long typeId) {
        List<PublicActivity> activities = publicActivityService.lambdaQuery()
                .eq(PublicActivity::getTypeId, typeId)
                .list();
        
        PublicActivityType type = publicActivityTypeService.getById(typeId);
        if (type != null) {
            for (PublicActivity activity : activities) {
                activity.setTypeName(type.getTypeName());
            }
        }
        
        return activities;
    }

    @GetMapping("/status/{status}")
    public List<PublicActivity> getActivitiesByStatus(@PathVariable Integer status) {
        List<PublicActivity> activities = publicActivityService.lambdaQuery()
                .eq(PublicActivity::getStatus, status)
                .list();
        
        // 获取所有活动类型
        Map<Long, String> typeMap = publicActivityTypeService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivityType::getId, PublicActivityType::getTypeName));
        
        // 为每个活动设置类型名称
        for (PublicActivity activity : activities) {
            activity.setTypeName(typeMap.get(activity.getTypeId()));
        }
        
        return activities;
    }

    @GetMapping("/search")
    public List<PublicActivity> searchActivities(@RequestParam String keyword) {
        List<PublicActivity> activities = publicActivityService.lambdaQuery()
                .and(wrapper -> wrapper
                        .like(PublicActivity::getActivityName, keyword)
                        .or()
                        .like(PublicActivity::getLocation, keyword))
                .list();
        
        // 获取所有活动类型
        Map<Long, String> typeMap = publicActivityTypeService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivityType::getId, PublicActivityType::getTypeName));
        
        // 为每个活动设置类型名称
        for (PublicActivity activity : activities) {
            activity.setTypeName(typeMap.get(activity.getTypeId()));
        }
        
        return activities;
    }
}