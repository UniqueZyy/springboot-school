package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.DonationProject;
import com.example.spring_school.mapper.DonationProjectMapper;
import com.example.spring_school.service.DonationProjectService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 捐赠项目表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@Service
public class DonationProjectServiceImpl extends ServiceImpl<DonationProjectMapper, DonationProject> implements DonationProjectService {

}