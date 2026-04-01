package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.User;
import com.example.spring_school.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员管理控制器
 */
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * 分页获取管理员
     */
    @GetMapping("/page")
    public Page<User> pageAdmins(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 只查询管理员角色（roleType=5）
        queryWrapper.eq("role_type", 5);
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("name", keyword).or().like("username", keyword).or().like("phone", keyword);
        }
        return userService.page(page, queryWrapper);
    }

    /**
     * 重置管理员密码
     */
    @PutMapping("/{id}/reset-password")
    public boolean resetAdminPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User admin = userService.getById(id);
        if (admin == null || admin.getRoleType() != 5) {
            return false;
        }
        admin.setPassword(request.get("newPassword"));
        return userService.updateById(admin);
    }

    /**
     * 添加管理员
     */
    @PostMapping
    public boolean createAdmin(@RequestBody User admin) {
        // 设置为管理员角色
        admin.setRoleType(5);
        return userService.save(admin);
    }

    /**
     * 更新管理员
     */
    @PutMapping("/{id}")
    public boolean updateAdmin(@PathVariable Long id, @RequestBody User admin) {
        admin.setId(id);
        // 确保角色类型为管理员
        admin.setRoleType(5);
        return userService.updateById(admin);
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{id}")
    public boolean deleteAdmin(@PathVariable Long id) {
        User admin = userService.getById(id);
        if (admin == null || admin.getRoleType() != 5) {
            return false;
        }
        return userService.removeById(id);
    }
}