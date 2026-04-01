package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.DonationType;
import com.example.spring_school.mapper.DonationTypeMapper;
import com.example.spring_school.service.DonationTypeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 捐赠物资类型表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-15
 */
@Service
public class DonationTypeServiceImpl extends ServiceImpl<DonationTypeMapper, DonationType> implements DonationTypeService {

}