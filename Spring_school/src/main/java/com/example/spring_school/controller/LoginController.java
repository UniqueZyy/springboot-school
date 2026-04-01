package com.example.spring_school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.spring_school.dto.LoginRequest;
import com.example.spring_school.dto.LoginResponse;
import com.example.spring_school.entity.User;
import com.example.spring_school.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();

        if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
            response.setCode(400);
            response.setMessage("用户名不能为空");
            return response;
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            response.setCode(400);
            response.setMessage("密码不能为空");
            return response;
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginRequest.getUsername());
        User user = userService.getOne(queryWrapper);

        if (user == null) {
            response.setCode(404);
            response.setMessage("用户不存在");
            return response;
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("账号已被禁用");
            return response;
        }

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            response.setCode(401);
            response.setMessage("密码错误");
            return response;
        }

        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        LoginResponse.UserVO userVO = new LoginResponse.UserVO();
        BeanUtils.copyProperties(user, userVO);

        response.setCode(200);
        response.setMessage("登录成功");
        response.setData(userVO);

        return response;
    }

    @PostMapping("/reset-password")
    public LoginResponse resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        LoginResponse response = new LoginResponse();

        if (resetPasswordRequest.getUsername() == null || resetPasswordRequest.getUsername().isEmpty()) {
            response.setCode(400);
            response.setMessage("用户名不能为空");
            return response;
        }

        if (resetPasswordRequest.getPhone() == null || resetPasswordRequest.getPhone().isEmpty()) {
            response.setCode(400);
            response.setMessage("手机号不能为空");
            return response;
        }

        if (resetPasswordRequest.getNewPassword() == null || resetPasswordRequest.getNewPassword().isEmpty()) {
            response.setCode(400);
            response.setMessage("新密码不能为空");
            return response;
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, resetPasswordRequest.getUsername());
        User user = userService.getOne(queryWrapper);

        if (user == null) {
            response.setCode(404);
            response.setMessage("用户不存在");
            return response;
        }

        if (!user.getPhone().equals(resetPasswordRequest.getPhone())) {
            response.setCode(400);
            response.setMessage("信息不匹配，修改失败，如有需要请联系学校管理员重置密码");
            return response;
        }

        user.setPassword(resetPasswordRequest.getNewPassword());
        userService.updateById(user);

        response.setCode(200);
        response.setMessage("密码重置成功");

        return response;
    }

    static class ResetPasswordRequest {
        private String username;
        private String phone;
        private String newPassword;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
