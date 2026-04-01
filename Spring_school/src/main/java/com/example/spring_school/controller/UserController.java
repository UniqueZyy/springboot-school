package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.spring_school.entity.User;
import com.example.spring_school.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.list();
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public boolean createUser(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public boolean updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateById(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable Long id) {
        return userService.removeById(id);
    }

    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    public Page<User> pageUsers(@RequestParam(defaultValue = "1") Integer current, 
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<User> page = new Page<>(current, size);
        return userService.page(page);
    }

    /**
     * 根据角色类型查询用户
     */
    @GetMapping("/role/{roleType}")
    public List<User> getUsersByRoleType(@PathVariable Integer roleType) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role_type", roleType);
        return userService.list(wrapper);
    }
}
