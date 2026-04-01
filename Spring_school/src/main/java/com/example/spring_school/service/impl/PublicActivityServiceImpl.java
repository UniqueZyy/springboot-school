package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.PublicActivity;
import com.example.spring_school.mapper.PublicActivityMapper;
import com.example.spring_school.service.PublicActivityService;
import org.springframework.stereotype.Service;

@Service
public class PublicActivityServiceImpl extends ServiceImpl<PublicActivityMapper, PublicActivity> implements PublicActivityService {
}
