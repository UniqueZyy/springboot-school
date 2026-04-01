package com.example.spring_school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 用户名（账号）
    private String username;

    // 密码
    private String password;

    // 姓名
    private String name;

    // 邮箱
    private String email;

    // 手机号
    private String phone;

    // 角色类型：1-学生，2-教师，3-校友，4-校外爱心人士，5-管理员
    private Integer roleType;

    // 学号/工号/校友编号
    private String code;

    // 性别：0-女，1-男
    private Integer gender;

    // 状态：0-禁用，1-启用
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 最后登录时间
    private LocalDateTime lastLoginTime;
}
