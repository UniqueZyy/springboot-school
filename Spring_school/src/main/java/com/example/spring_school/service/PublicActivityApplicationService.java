package com.example.spring_school.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.spring_school.entity.PublicActivityApplication;

import java.util.List;

public interface PublicActivityApplicationService extends IService<PublicActivityApplication> {
    List<PublicActivityApplication> listByStatus(int status);
}
