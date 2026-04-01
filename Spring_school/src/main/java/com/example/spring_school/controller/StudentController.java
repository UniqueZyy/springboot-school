package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.User;
import com.example.spring_school.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学生管理控制器
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private UserService userService;

    /**
     * 分页获取学生
     */
    @GetMapping("/page")
    public Page<User> pageStudents(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String classNo) {
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 只查询学生角色（roleType=1）
        queryWrapper.eq("role_type", 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("name", keyword).or().like("username", keyword).or().like("phone", keyword).or().like("code", keyword);
        }
        // 这里可以根据实际需求添加年级和班级的查询条件
        // 假设code字段存储了学号，包含年级和班级信息
        if (grade != null && !grade.trim().isEmpty()) {
            queryWrapper.like("code", grade);
        }
        if (classNo != null && !classNo.trim().isEmpty()) {
            queryWrapper.like("code", classNo);
        }
        return userService.page(page, queryWrapper);
    }

    /**
     * 添加学生
     */
    @PostMapping
    public boolean createStudent(@RequestBody User student) {
        // 设置为学生角色
        student.setRoleType(1);
        return userService.save(student);
    }

    /**
     * 更新学生
     */
    @PutMapping("/{id}")
    public boolean updateStudent(@PathVariable Long id, @RequestBody User student) {
        student.setId(id);
        // 确保角色类型为学生
        student.setRoleType(1);
        return userService.updateById(student);
    }

    /**
     * 删除学生
     */
    @DeleteMapping("/{id}")
    public boolean deleteStudent(@PathVariable Long id) {
        User student = userService.getById(id);
        if (student == null || student.getRoleType() != 1) {
            return false;
        }
        return userService.removeById(id);
    }
}