package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.UserRole;
import com.example.spring_school.mapper.UserRoleMapper;
import com.example.spring_school.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户角色关系服务实现类
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
