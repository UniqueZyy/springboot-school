package com.example.spring_school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.spring_school.entity.FundDonation;
import com.example.spring_school.mapper.FundDonationMapper;
import com.example.spring_school.service.FundDonationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundDonationServiceImpl extends ServiceImpl<FundDonationMapper, FundDonation> implements FundDonationService {
    @Override
    public List<FundDonation> listByStatus(int status) {
        QueryWrapper<FundDonation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return baseMapper.selectList(queryWrapper);
    }
}
