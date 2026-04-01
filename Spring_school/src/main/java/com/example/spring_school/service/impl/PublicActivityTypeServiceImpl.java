package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.PublicActivityType;
import com.example.spring_school.mapper.PublicActivityTypeMapper;
import com.example.spring_school.service.PublicActivityTypeService;
import org.springframework.stereotype.Service;

@Service
public class PublicActivityTypeServiceImpl extends ServiceImpl<PublicActivityTypeMapper, PublicActivityType> implements PublicActivityTypeService {
}
