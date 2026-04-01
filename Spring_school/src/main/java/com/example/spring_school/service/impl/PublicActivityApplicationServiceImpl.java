package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.PublicActivityApplication;
import com.example.spring_school.mapper.PublicActivityApplicationMapper;
import com.example.spring_school.service.PublicActivityApplicationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicActivityApplicationServiceImpl extends ServiceImpl<PublicActivityApplicationMapper, PublicActivityApplication> implements PublicActivityApplicationService {
    @Override
    public List<PublicActivityApplication> listByStatus(int status) {
        QueryWrapper<PublicActivityApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return baseMapper.selectList(queryWrapper);
    }
}
