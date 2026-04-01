# 高校爱心捐赠系统数据库初始化

## 1. 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS spring_school CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE spring_school;
```

## 2. 创建数据表

### 2.1 角色表(role)
```sql
CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_type` int(11) NOT NULL COMMENT '角色类型：1-学生，2-教师，3-校友，4-校外爱心人士，5-管理员',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `permissions` varchar(500) DEFAULT NULL COMMENT '权限标识（JSON格式）',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_type` (`role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

### 2.2 用户表(user)
```sql
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（加密存储）',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role_type` int(11) NOT NULL COMMENT '角色类型：1-学生，2-教师，3-校友，4-校外爱心人士，5-管理员',
  `code` varchar(50) DEFAULT NULL COMMENT '学号/工号/校友编号',
  `gender` int(11) DEFAULT NULL COMMENT '性别：0-女，1-男',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_type` (`role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 2.3 用户角色关联表(user_role)
```sql
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

### 2.4 捐赠项目表(donation_project)
```sql
CREATE TABLE IF NOT EXISTS `donation_project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `description` text COMMENT '项目描述',
  `target_amount` decimal(15,2) NOT NULL COMMENT '目标金额',
  `current_amount` decimal(15,2) NOT NULL DEFAULT 0.00 COMMENT '当前金额',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态：1-进行中，2-已完成，3-已关闭',
  `creator_id` bigint(20) NOT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='捐赠项目表';
```

### 2.5 捐赠记录表(donation_record)
```sql
CREATE TABLE IF NOT EXISTS `donation_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '捐赠用户ID',
  `project_id` bigint(20) NOT NULL COMMENT '捐赠项目ID',
  `amount` decimal(15,2) NOT NULL COMMENT '捐赠金额',
  `donation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捐赠时间',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `anonymous` int(11) NOT NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_donation_time` (`donation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='捐赠记录表';
```

## 3. 插入初始数据

### 3.1 角色数据
```sql
INSERT INTO `role` (`role_name`, `role_type`, `description`, `permissions`, `status`) VALUES
('学生', 1, '在校学生角色，可查看项目、进行捐赠', '["project:view","donation:create"]', 1),
('教师', 2, '学校教师角色，可查看项目、进行捐赠、管理学生', '["project:view","donation:create","student:manage"]', 1),
('校友', 3, '校友角色，可查看项目、进行捐赠、管理个人信息', '["project:view","donation:create","profile:manage"]', 1),
('校外爱心人士', 4, '校外爱心人士角色，可查看项目、进行捐赠', '["project:view","donation:create"]', 1),
('管理员', 5, '系统管理员角色，拥有所有权限', '["*:*"]', 1);
```

### 3.2 用户数据（每个角色一条）
```sql
-- 密码均为123456（MD5加密）
INSERT INTO `user` (`username`, `password`, `name`, `email`, `phone`, `role_type`, `code`, `gender`, `status`) VALUES
('student001', 'e10adc3949ba59abbe56e057f20f883e', '张三', 'zhangsan@example.com', '13800138001', 1, '20210001', 1, 1),
('teacher001', 'e10adc3949ba59abbe56e057f20f883e', '李四', 'lisi@example.com', '13800138002', 2, 'T001', 1, 1),
('alumni001', 'e10adc3949ba59abbe56e057f20f883e', '王五', 'wangwu@example.com', '13800138003', 3, 'A2015001', 0, 1),
('donor001', 'e10adc3949ba59abbe56e057f20f883e', '赵六', 'zhaoliu@example.com', '13800138004', 4, NULL, 1, 1),
('admin001', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'admin@example.com', '13800138005', 5, NULL, 1, 1);
```

### 3.3 用户角色关联数据
```sql
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);
```

### 3.4 捐赠项目数据
```sql
INSERT INTO `donation_project` (`project_name`, `description`, `target_amount`, `current_amount`, `start_time`, `end_time`, `status`, `creator_id`) VALUES
('贫困学生助学金', '为家庭经济困难的学生提供助学金支持', 50000.00, 0.00, NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 1, 5),
('校园图书角建设', '为学校各班级建设图书角，丰富学生阅读资源', 30000.00, 0.00, NOW(), DATE_ADD(NOW(), INTERVAL 2 MONTH), 1, 5);
```

### 3.5 捐赠记录数据
```sql
INSERT INTO `donation_record` (`user_id`, `project_id`, `amount`, `donation_time`, `remark`, `anonymous`) VALUES
(1, 1, 100.00, NOW(), '支持贫困学生', 0),
(2, 1, 500.00, NOW(), '尽一份力', 0),
(3, 2, 200.00, NOW(), '支持校园建设', 0),
(4, 2, 1000.00, NOW(), '希望孩子们多读书', 1),
(5, 1, 2000.00, NOW(), '管理员捐赠', 0);
```

## 4. 数据库使用说明

1. 执行以上SQL语句前，请确保MySQL服务已启动
2. 先创建数据库，然后创建表结构，最后插入初始数据
3. 默认用户名和密码：
   - 学生：student001 / 123456
   - 教师：teacher001 / 123456
   - 校友：alumni001 / 123456
   - 校外爱心人士：donor001 / 123456
   - 管理员：admin001 / 123456
4. 密码采用MD5加密，实际生产环境建议使用更安全的加密方式（如BCrypt）