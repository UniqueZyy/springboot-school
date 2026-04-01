package com.example.spring_school.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.PublicActivity;
import com.example.spring_school.entity.PublicActivityApplication;
import com.example.spring_school.entity.User;
import com.example.spring_school.service.PublicActivityApplicationService;
import com.example.spring_school.service.PublicActivityService;
import com.example.spring_school.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
public class PublicActivityApplicationController {

    @Autowired
    private PublicActivityApplicationService publicActivityApplicationService;
    
    @Autowired
    private PublicActivityService publicActivityService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public List<PublicActivityApplication> getAllApplications() {
        List<PublicActivityApplication> applications = publicActivityApplicationService.list();
        
        // 获取所有活动和用户信息
        Map<Long, String> activityNameMap = publicActivityService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivity::getId, PublicActivity::getActivityName));
        
        Map<Long, String> userNameMap = userService.list()
                .stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        
        Map<Long, Long> activityCreatorMap = publicActivityService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivity::getId, PublicActivity::getCreatorId));
        
        Map<Long, String> creatorNameMap = userService.list()
                .stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        
        Map<Long, Integer> activityStatusMap = publicActivityService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivity::getId, PublicActivity::getStatus));
        
        // 为每个申请设置关联信息
        for (PublicActivityApplication application : applications) {
            application.setActivityName(activityNameMap.get(application.getActivityId()));
            application.setUserName(userNameMap.get(application.getUserId()));
            
            Long creatorId = activityCreatorMap.get(application.getActivityId());
            if (creatorId != null) {
                application.setActivityCreatorName(creatorNameMap.get(creatorId));
            }
            
            application.setActivityStatus(activityStatusMap.get(application.getActivityId()));
        }
        
        return applications;
    }

    @GetMapping("/{id}")
    public PublicActivityApplication getApplicationById(@PathVariable Long id) {
        PublicActivityApplication application = publicActivityApplicationService.getById(id);
        if (application != null) {
            // 设置关联信息
            setRelatedInfo(application);
        }
        return application;
    }

    @PostMapping
    public boolean createApplication(@RequestBody PublicActivityApplication application) {
        // 设置默认值
        if (application.getStatus() == null) {
            application.setStatus(0); // 0表示待审批
        }
        if (application.getApplyTime() == null) {
            application.setApplyTime(LocalDateTime.now());
        }
        return publicActivityApplicationService.save(application);
    }

    @PutMapping("/{id}")
    public boolean updateApplication(@PathVariable Long id, @RequestBody PublicActivityApplication application) {
        application.setId(id);
        return publicActivityApplicationService.updateById(application);
    }

    @DeleteMapping("/{id}")
    public boolean deleteApplication(@PathVariable Long id) {
        return publicActivityApplicationService.removeById(id);
    }

    @GetMapping("/page")
    public Page<PublicActivityApplication> pageApplications(@RequestParam(defaultValue = "1") Integer current,
                                                              @RequestParam(defaultValue = "10") Integer size,
                                                              @RequestParam(required = false) String keyword,
                                                              @RequestParam(required = false) String status) {
        Page<PublicActivityApplication> page = new Page<>(current, size);
        
        // 获取所有活动和用户信息
        Map<Long, String> activityNameMap = publicActivityService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivity::getId, PublicActivity::getActivityName));
        
        Map<Long, String> userNameMap = userService.list()
                .stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        
        Map<Long, Long> activityCreatorMap = publicActivityService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivity::getId, PublicActivity::getCreatorId));
        
        Map<Long, String> creatorNameMap = userService.list()
                .stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        
        Map<Long, Integer> activityStatusMap = publicActivityService.list()
                .stream()
                .collect(Collectors.toMap(PublicActivity::getId, PublicActivity::getStatus));
        
        // 使用 QueryWrapper 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PublicActivityApplication> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        // 添加状态搜索条件
        if (status != null && !status.trim().isEmpty()) {
            try {
                Integer statusValue = Integer.parseInt(status);
                queryWrapper.eq("status", statusValue);
            } catch (NumberFormatException ignored) {
                // ignore invalid status
            }
        }
        
        // 先执行分页查询（不包含关键词搜索）
        Page<PublicActivityApplication> result = publicActivityApplicationService.page(page, queryWrapper);
        
        // 为每页的申请设置关联信息
        for (PublicActivityApplication application : result.getRecords()) {
            application.setActivityName(activityNameMap.get(application.getActivityId()));
            application.setUserName(userNameMap.get(application.getUserId()));
            
            Long creatorId = activityCreatorMap.get(application.getActivityId());
            if (creatorId != null) {
                application.setActivityCreatorName(creatorNameMap.get(creatorId));
            }
            
            application.setActivityStatus(activityStatusMap.get(application.getActivityId()));
        }
        
        // 如果有关键词搜索，在前端过滤结果
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<PublicActivityApplication> filteredRecords = result.getRecords().stream()
                    .filter(app -> 
                            (app.getActivityName() != null && app.getActivityName().contains(keyword)) ||
                            (app.getApplicationReason() != null && app.getApplicationReason().contains(keyword)))
                    .collect(Collectors.toList());
            
            result.setRecords(filteredRecords);
            result.setTotal((long) filteredRecords.size());
        }
        
        return result;
    }

    @GetMapping("/user/{userId}")
    public Page<PublicActivityApplication> getApplicationsByUserId(@PathVariable Long userId,
                                                                  @RequestParam(defaultValue = "1") Integer current,
                                                                  @RequestParam(defaultValue = "10") Integer size,
                                                                  @RequestParam(required = false) String status) {
        Page<PublicActivityApplication> page = new Page<>(current, size);
        
        // 使用 QueryWrapper 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PublicActivityApplication> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        
        // 添加状态搜索条件
        if (status != null && !status.trim().isEmpty()) {
            try {
                Integer statusValue = Integer.parseInt(status);
                queryWrapper.eq("status", statusValue);
            } catch (NumberFormatException ignored) {
                // ignore invalid status
            }
        }
        
        // 执行分页查询
        Page<PublicActivityApplication> result = publicActivityApplicationService.page(page, queryWrapper);
        
        // 为每页的申请设置关联信息
        for (PublicActivityApplication application : result.getRecords()) {
            setRelatedInfo(application);
        }
        
        return result;
    }

    @GetMapping("/activity/{activityId}")
    public List<PublicActivityApplication> getApplicationsByActivityId(@PathVariable Long activityId) {
        List<PublicActivityApplication> applications = publicActivityApplicationService.lambdaQuery()
                .eq(PublicActivityApplication::getActivityId, activityId)
                .list();
        
        // 设置关联信息
        for (PublicActivityApplication application : applications) {
            setRelatedInfo(application);
        }
        
        return applications;
    }

    @GetMapping("/status/{status}")
    public List<PublicActivityApplication> getApplicationsByStatus(@PathVariable Integer status) {
        List<PublicActivityApplication> applications = publicActivityApplicationService.lambdaQuery()
                .eq(PublicActivityApplication::getStatus, status)
                .list();
        
        // 设置关联信息
        for (PublicActivityApplication application : applications) {
            setRelatedInfo(application);
        }
        
        return applications;
    }

    @PutMapping("/{id}/approve")
    public boolean approveApplication(@PathVariable Long id, 
                                      @RequestParam Long approverId,
                                      @RequestParam(required = false) String approveRemark) {
        PublicActivityApplication application = publicActivityApplicationService.getById(id);
        if (application == null) {
            return false;
        }
        application.setStatus(1);
        application.setApproveTime(LocalDateTime.now());
        application.setApproverId(approverId);
        application.setApproveRemark(approveRemark);
        return publicActivityApplicationService.updateById(application);
    }

    @PutMapping("/{id}/reject")
    public boolean rejectApplication(@PathVariable Long id,
                                     @RequestParam Long approverId,
                                     @RequestParam(required = false) String approveRemark) {
        PublicActivityApplication application = publicActivityApplicationService.getById(id);
        if (application == null) {
            return false;
        }
        application.setStatus(2);
        application.setApproveTime(LocalDateTime.now());
        application.setApproverId(approverId);
        application.setApproveRemark(approveRemark);
        return publicActivityApplicationService.updateById(application);
    }

    @PutMapping("/{id}/cancel")
    public boolean cancelApplication(@PathVariable Long id) {
        PublicActivityApplication application = publicActivityApplicationService.getById(id);
        if (application == null) {
            return false;
        }
        // 只有待审批状态的申请才能取消
        if (application.getStatus() != 0) {
            return false;
        }
        application.setStatus(3); // 3表示已取消
        application.setApproveTime(LocalDateTime.now());
        return publicActivityApplicationService.updateById(application);
    }

    @GetMapping("/search")
    public List<PublicActivityApplication> searchApplications(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        var queryWrapper = publicActivityApplicationService.lambdaQuery();
        
        // 添加关键词搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(PublicActivityApplication::getApplicationReason, keyword)
                    .or()
                    .eq(PublicActivityApplication::getUserId, keyword)
                    .or()
                    .eq(PublicActivityApplication::getActivityId, keyword));
        }
        
        // 添加状态搜索条件
        if (status != null) {
            queryWrapper.eq(PublicActivityApplication::getStatus, status);
        }
        
        List<PublicActivityApplication> applications = queryWrapper.list();
        
        // 设置关联信息
        for (PublicActivityApplication application : applications) {
            setRelatedInfo(application);
        }
        
        return applications;
    }
    
    // 辅助方法：设置关联信息
    private void setRelatedInfo(PublicActivityApplication application) {
        if (application == null) return;
        
        PublicActivity activity = publicActivityService.getById(application.getActivityId());
        if (activity != null) {
            application.setActivityName(activity.getActivityName());
            // 确保活动状态被正确设置，默认为0（未开始）
            Integer activityStatus = activity.getStatus();
            application.setActivityStatus(activityStatus != null ? activityStatus : 0);
            // 设置活动开始时间和结束时间
            application.setActivityStartTime(activity.getStartTime() != null ? activity.getStartTime().toString() : null);
            application.setActivityEndTime(activity.getEndTime() != null ? activity.getEndTime().toString() : null);
            
            User creator = userService.getById(activity.getCreatorId());
            if (creator != null) {
                application.setActivityCreatorName(creator.getName());
            }
        } else {
            // 如果活动不存在，默认设置为未开始
            application.setActivityStatus(0);
        }
        
        User user = userService.getById(application.getUserId());
        if (user != null) {
            application.setUserName(user.getName());
        }
    }
}